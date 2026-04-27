package dev.ia.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * Responsável por carregar e indexar os documentos de pacotes de viagem
 * no repositório de embeddings durante a inicialização da aplicação.
 * <p>
 * Este processo alimenta o pipeline RAG com o conteúdo que o assistente
 * utilizará para responder às perguntas dos usuários.
 */
@ApplicationScoped
public class DocumentIngester {

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    @Inject
    EmbeddingModel embeddingModel;

    /**
     * Executa automaticamente na inicialização da aplicação (observa {@link StartupEvent}).
     * <p>
     * Realiza as seguintes etapas:
     * <ol>
     *   <li>Carrega o arquivo {@code src/main/resources/rag/pakotes-viagem.md} do sistema de arquivos.</li>
     *   <li>Adiciona o metadado {@code type=packages} ao documento para facilitar filtragens futuras.</li>
     *   <li>Divide o documento em chunks de {@code 200} tokens com sobreposição de {@code 20} tokens,
     *       preservando a coerência semântica entre os trechos.</li>
     *   <li>Gera embeddings para cada chunk e os armazena no {@link EmbeddingStore}.</li>
     * </ol>
     *
     * @param event evento de startup do Quarkus (injetado automaticamente pelo CDI)
     */
    public void onStart(@Observes StartupEvent event) {
        Document document = ClassPathDocumentLoader.loadDocument("rag/pakotes-viagem.md");

        document.metadata().put("type", "packages");

        DocumentSplitter splitter = DocumentSplitters.recursive(200, 20);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(document);
    }
}
