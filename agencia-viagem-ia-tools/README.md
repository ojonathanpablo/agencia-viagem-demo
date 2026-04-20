# agencia-viagem-ia-tools

Assistente virtual de agência de viagens construído com Quarkus e LangChain4j, utilizando RAG (Retrieval-Augmented Generation) com modelos de IA locais via Ollama e banco de vetores pgvector.

## Pré-requisitos

- Java 17+
- Maven 3.9+
- [Ollama](https://ollama.com/download)
- Docker (o Quarkus sobe o pgvector automaticamente em dev mode)

---

## 1. Instalar e configurar o Ollama

Baixe e instale o Ollama em: https://ollama.com/download

Após instalar, baixe os modelos necessários:

**Modelo de linguagem (LLM):**
```shell
ollama pull gemma3:4b
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

## 2. Executar a aplicação

Com o Ollama rodando e o Docker ativo, inicie a aplicação em modo dev:

```shell
./mvnw quarkus:dev
```

A aplicação estará disponível em: http://localhost:8080

Dev UI disponível em: http://localhost:8080/q/dev/

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
