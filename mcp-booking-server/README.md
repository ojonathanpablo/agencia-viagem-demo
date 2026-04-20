# mcp-booking-server

Servidor MCP (Model Context Protocol) de gerenciamento de reservas de viagem, construído com Quarkus. Expõe ferramentas via SSE que podem ser invocadas por clientes LangChain4j (como o `travel-agency-ai`) durante a geração de respostas do LLM.

## Pré-requisitos

- Java 21+
- Maven 3.9+

> Não requer Docker nem Ollama — os dados são mantidos em memória.

---

## 1. Executar a aplicação

```shell
./mvnw quarkus:dev
```

O servidor MCP estará disponível em: http://localhost:8081

Endpoint SSE (usado pelo cliente LangChain4j): http://localhost:8081/mcp/sse/

Dev UI disponível em: http://localhost:8081/q/dev/

---

## 2. Ferramentas MCP disponíveis

| Ferramenta | Descrição |
|---|---|
| `Lista todas as reservas de viagem de um usuário específico.` | Retorna todas as reservas de um cliente pelo nome |
| `Obtém os detalhes completos de uma reserva com base em seu número de identificação (bookingId).` | Busca uma reserva pelo ID |
| `Cria uma nova reserva de viagem para o usuário autenticado.` | Cria uma reserva com destino, datas e categoria |
| `Cancela uma reserva existente com base no seu ID (bookingId).` | Cancela uma reserva, validando o titular |
| `Lista os pacotes de viagem disponíveis para uma determinada categoria (ex: ADVENTURE, TREASURES).` | Lista destinos disponíveis por categoria |

---

## 3. Dados de exemplo pré-carregados

O servidor inicializa com três reservas de demonstração:

| ID | Cliente | Destino | Categoria | Status |
|---|---|---|---|---|
| 12345 | John Doe | Tesouros do Egito | TREASURES | CONFIRMED |
| 67890 | Jane Smith | Aventura Amazônia | ADVENTURE | CONFIRMED |
| 98765 | Peter Jones | Trilha Inca | ADVENTURE | CONFIRMED |

> Os dados são armazenados em memória — ao reiniciar, voltam ao estado inicial.

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

- [Quarkus MCP Server](https://docs.quarkiverse.io/quarkus-mcp-server/dev/index.html)
- [Quarkus REST](https://quarkus.io/guides/rest)