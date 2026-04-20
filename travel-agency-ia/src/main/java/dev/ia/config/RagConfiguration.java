package dev.ia.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Configuração do pipeline RAG (Retrieval-Augmented Generation).
 * <p>
 * Define como o assistente de IA busca informações relevantes nos documentos
 * indexados antes de gerar uma resposta, garantindo que as respostas sejam
 * baseadas no conteúdo real dos pacotes de viagem.
 */
@ApplicationScoped
public class RagConfiguration {

    /**
     * Cria e configura o {@link RetrievalAugmentor} responsável por enriquecer
     * as perguntas dos usuários com contexto extraído dos documentos indexados.
     * <p>
     * O processo funciona da seguinte forma:
     * <ol>
     *   <li>A pergunta do usuário é convertida em um vetor (embedding) pelo {@code embeddingModel}.</li>
     *   <li>O {@code embeddingStore} realiza uma busca por similaridade para encontrar os trechos mais relevantes.</li>
     *   <li>Os {@code maxResults(6)} trechos mais similares são injetados como contexto na requisição ao LLM.</li>
     * </ol>
     *
     * @param embeddingStore repositório onde os vetores dos documentos estão armazenados
     * @param embeddingModel modelo responsável por transformar textos em vetores
     * @return uma instância configurada de {@link RetrievalAugmentor}
     */
    public RetrievalAugmentor retrievalAugmentor(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(EmbeddingStoreContentRetriever.builder()
                        .embeddingStore(embeddingStore)
                        .embeddingModel(embeddingModel)
                        .maxResults(6)
                        .build())
                .build();
    }

}
