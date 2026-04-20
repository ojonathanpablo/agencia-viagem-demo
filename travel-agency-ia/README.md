# travel-agency-ai

Assistente virtual de agência de viagens construído com Quarkus e LangChain4j, utilizando RAG (Retrieval-Augmented Generation), modelos de IA locais via Ollama, banco de vetores pgvector e ferramentas de reserva expostas via protocolo MCP.

## Pré-requisitos

- Java 21+
- Maven 3.9+
- [Ollama](https://ollama.com/download)
- Docker (o Quarkus sobe o pgvector automaticamente em dev mode)
- **`mcp-booking-server` rodando na porta `8081`** (veja a seção abaixo)

---

## 1. Instalar e configurar o Ollama

Baixe e instale o Ollama em: https://ollama.com/download

Após instalar, baixe os modelos necessários:

**Modelo de linguagem (LLM):**
```shell
ollama pull gpt-oss:20b
```

**Modelo de embeddings (RAG):**
```shell
ollama pull nomic-embed-text
```

Após baixar os modelos, inicie o servidor do Ollama:

```shell
ollama serve
```

---

## 2. Iniciar o servidor MCP (mcp-booking-server)

O `travel-agency-ai` depende do `mcp-booking-server` para executar as ferramentas de reserva (criar, cancelar, listar reservas etc.). Ele deve estar rodando **antes** de iniciar esta aplicação.

Na pasta `mcp-booking-server`, execute:

```shell
./mvnw quarkus:dev
```

O servidor MCP estará disponível em: http://localhost:8081/mcp/sse/

---

## 3. Executar a aplicação

Com o Ollama rodando, o Docker ativo e o `mcp-booking-server` iniciado, execute em modo dev:

```shell
./mvnw quarkus:dev
```

A aplicação estará disponível em: http://localhost:8080

Dev UI disponível em: http://localhost:8080/q/dev/

---

## 4. Usar o assistente

Envie uma mensagem ao assistente via `POST /travel`, passando o nome do usuário autenticado no cabeçalho `X-User-Name`:

```shell
curl -X POST http://localhost:8080/travel \
  -H "Content-Type: text/plain" \
  -H "X-User-Name: John Doe" \
  -d "Quais são minhas reservas?"
```

---

## Empacotamento

```shell
./mvnw package
```

Gera o arquivo `target/quarkus-app/quarkus-run.jar`. Para executar:

```shell
java -jar target/quarkus-app/quarkus-run.jar
```

---

## Guias relacionados

- [Quarkus REST](https://quarkus.io/guides/rest)
- [Quarkus LangChain4j + Ollama](https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html)
- [Quarkus LangChain4j MCP](https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html)
- [Quarkus LangChain4j pgvector](https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html)