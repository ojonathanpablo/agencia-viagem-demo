package dev.ia.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;

/**
 * Configuração do mecanismo de memória de conversação do assistente de IA.
 * <p>
 * Produz um bean CDI do tipo {@link ChatMemory} que é injetado automaticamente
 * pelo Quarkus LangChain4j em cada sessão de chat ativa.
 */
@ApplicationScoped
public class ChatMemoryConfig {

    /**
     * Cria e configura a memória de chat para cada sessão de conversa.
     * <p>
     * Utiliza uma janela deslizante de mensagens ({@link MessageWindowChatMemory}),
     * mantendo apenas as últimas {@code 20} mensagens em memória. Quando o limite
     * é atingido, as mensagens mais antigas são descartadas automaticamente.
     * O armazenamento é feito em memória ({@link InMemoryChatMemoryStore}),
     * ou seja, os dados são perdidos ao reiniciar a aplicação.
     *
     * @return uma instância configurada de {@link ChatMemory}
     */
    @Produces
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .chatMemoryStore(new InMemoryChatMemoryStore())
                .build();
    }
}
