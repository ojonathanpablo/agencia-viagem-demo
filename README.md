# Agência de Viagens — Demo com IA

Este repositório demonstra a evolução de uma aplicação de assistente virtual para uma agência de viagens, partindo de uma **arquitetura monolítica** até uma **arquitetura de microserviços** com comunicação via protocolo MCP.

---

## Visão Geral dos Projetos

```
agencia-viagem-demo/
├── agencia-viagem-ia-tools/   # Monolito — IA + ferramentas de reserva em um só serviço
├── mcp-booking-server/        # Microserviço — servidor MCP de gerenciamento de reservas
└── travel-agency-ia/          # Microserviço — assistente de IA que consome o MCP
```

---

## Arquitetura

### Monolito: `agencia-viagem-ia-tools`

Nesta versão, **tudo roda em um único processo**. O LLM tem acesso direto às ferramentas de reserva, que são beans CDI injetados na mesma JVM.

```
┌──────────────────────────────────────────────┐
│           agencia-viagem-ia-tools             │
│                                              │
│  HTTP ──► TravelAgentResource                │
│                │                             │
│           PackageExpert (AI Service)         │
│           ├── RAG (pgvector + embeddings)    │
│           ├── ChatMemory                     │
│           └── BookingTools ◄─ injeto no LLM  │
│                    └── BookingService        │
│                         └── dados em memória │
└──────────────────────────────────────────────┘
```

**Fluxo:**
1. Cliente envia mensagem via `POST /travel`
2. O LLM consulta documentos indexados via RAG para enriquecer o contexto
3. Se necessário, invoca diretamente os métodos de `BookingTools` (criar/cancelar/listar reservas)
4. Retorna a resposta ao cliente

**Tecnologias:**
- Quarkus + LangChain4j
- Ollama (`gemma3:4b` + `nomic-embed-text`)
- pgvector (sobe automaticamente via Docker em dev mode)

**Porta:** `8080`

---

### Microserviços: `mcp-booking-server` + `travel-agency-ia`

Nesta versão, as responsabilidades são **separadas em dois serviços independentes** que se comunicam via **MCP (Model Context Protocol)** sobre SSE (Server-Sent Events).

```
                     ┌────────────────────────────────────────────┐
                     │           travel-agency-ia  (:8080)        │
                     │                                            │
HTTP ──► TravelAgentResource                                      │
              │      │                                            │
         TravelAssistant (AI Service)                             │
         ├── InjectionGuard (input guardrail)                     │
         ├── ToneGuardrail  (output guardrail)                    │
         ├── JsonStructureGuard (output guardrail)                │
         ├── RAG (pgvector + nomic-embed-text)                    │
         ├── ChatMemory                                           │
         └── McpToolBox("booking-server") ──SSE──►┐              │
                     │                            │              │
                     └────────────────────────────┘              │
                                                  │
                     ┌────────────────────────────▼──────────────┐
                     │         mcp-booking-server  (:8081)       │
                     │                                           │
                     │  BookingTools (ferramentas MCP)           │
                     │  └── BookingService                       │
                     │       └── dados em memória                │
                     └───────────────────────────────────────────┘
```

**Fluxo:**
1. Cliente envia mensagem via `POST /travel` com cabeçalho `X-User-Name`
2. `InjectionGuard` analisa a mensagem procurando ataques de prompt injection — bloqueia se detectado
3. O LLM enriquece a resposta com contexto via RAG (documentos indexados no pgvector)
4. Se necessário, o LLM invoca ferramentas remotas no `mcp-booking-server` via protocolo MCP/SSE
5. `ToneGuardrail` valida o tom da resposta — solicita reescrita se for inadequado (máx. 3 tentativas)
6. Resposta entregue ao cliente

Para o endpoint `GET /travel/packages/{category}`:
1. O LLM gera a lista de pacotes em formato JSON
2. `JsonStructureGuard` valida se é um JSON válido — solicita reescrita se não for (máx. 3 tentativas)
3. JSON retornado ao cliente

---

## Protocolo MCP (Model Context Protocol)

O MCP é o protocolo que permite ao LLM do `travel-agency-ia` invocar ferramentas que rodam em outro processo (`mcp-booking-server`), como se fossem funções locais.

```
travel-agency-ia                     mcp-booking-server
      │                                      │
      │── GET /mcp/sse/ ───────────────────► │  (handshake SSE)
      │◄─ stream de eventos ─────────────────│
      │                                      │
      │── POST /mcp/messages/{session} ─────►│  (invoca ferramenta)
      │       { "method": "tools/call",      │
      │         "params": { "name":          │
      │           "getBookingsForUser" } }   │
      │◄─ { "result": [...] } ───────────────│  (retorno)
```

**Ferramentas expostas pelo `mcp-booking-server`:**

| Ferramenta | Descrição |
|---|---|
| `getBookingsForUser` | Lista todas as reservas de um usuário |
| `getBookingById` | Busca detalhes de uma reserva pelo ID |
| `createBooking` | Cria uma nova reserva |
| `cancelBooking` | Cancela uma reserva existente |
| `getAvailablePackages` | Lista pacotes disponíveis por categoria |

---

## Guardrails (Proteções do `travel-agency-ia`)

Guardrails são interceptadores que validam a entrada e saída do LLM antes de chegar ao usuário.

| Guardrail | Tipo | O que faz |
|---|---|---|
| `InjectionGuard` | Input | Bloqueia mensagens com tentativas de prompt injection |
| `ToneGuardrail` | Output | Garante tom profissional nas respostas (reprompt se rude) |
| `JsonStructureGuard` | Output | Garante que a resposta seja um JSON válido (reprompt se inválido) |

Máximo de tentativas de reprompt: **3** (configurado em `application.properties`).

---

## Como Executar

### Pré-requisitos comuns

- Java 21+
- Maven 3.9+
- Docker
- [Ollama](https://ollama.com/download)

### 1. Instalar modelos no Ollama

```shell
# Modelo de linguagem
ollama pull gpt-oss:20b

# Modelo de embeddings
ollama pull nomic-embed-text

# Iniciar o servidor Ollama
ollama serve
```

### 2. Iniciar o `mcp-booking-server`

```shell
cd mcp-booking-server
./mvnw quarkus:dev
```

Disponível em: `http://localhost:8081`

### 3. Iniciar o `travel-agency-ia`

Com o Ollama e o `mcp-booking-server` rodando:

```shell
cd travel-agency-ia
./mvnw quarkus:dev
```

Disponível em: `http://localhost:8080`

### 4. Testar

**Chat com o assistente:**
```shell
curl -X POST http://localhost:8080/travel \
  -H "Content-Type: text/plain" \
  -H "X-User-Name: John Doe" \
  -d "Quais são minhas reservas?"
```

**Listar pacotes por categoria:**
```shell
curl http://localhost:8080/travel/packages/ADVENTURE
```

---

### Monolito (opcional)

Para rodar a versão monolítica independente:

```shell
# Instalar modelo (gemma3:4b é o padrão desta versão)
ollama pull gemma3:4b

cd agencia-viagem-ia-tools
./mvnw quarkus:dev
```

Disponível em: `http://localhost:8080`

---

## Comparativo: Monolito vs Microserviços

| Aspecto | Monolito (`agencia-viagem-ia-tools`) | Microserviços |
|---|---|---|
| Deploy | Um único processo | Dois processos independentes |
| Ferramentas do LLM | Beans CDI na mesma JVM | Servidor remoto via MCP/SSE |
| Escalabilidade | Escala o serviço inteiro | Escala IA e booking separadamente |
| Guardrails | Não possui | Input + Output guardrails |
| Complexidade operacional | Baixa | Média (dois serviços para subir) |
| Isolamento de falhas | Não — falha no booking derruba tudo | Sim — booking pode falhar sem derrubar a IA |