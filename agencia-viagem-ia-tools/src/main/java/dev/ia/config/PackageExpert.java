package dev.ia.config;

import dev.ia.tools.BookingTools;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

/**
 * Serviço de IA responsável por atender os clientes da agência 'Mundo Viagens'.
 * <p>
 * Registrado como um AI Service do Quarkus LangChain4j via {@link RegisterAiService},
 * com acesso às ferramentas de reserva definidas em {@link BookingTools}.
 * O pipeline RAG configurado em {@code RagConfiguration} é aplicado automaticamente,
 * enriquecendo cada pergunta com contexto extraído dos documentos indexados.
 */
@RegisterAiService(tools = BookingTools.class)
public interface PackageExpert {

    /**
     * Processa uma mensagem do usuário e retorna a resposta do assistente.
     * <p>
     * O {@code @SystemMessage} define o comportamento e as restrições do assistente:
     * responder apenas com base nos documentos fornecidos, sem inventar informações.
     * Caso a resposta não esteja disponível nos documentos, o assistente deve
     * informar educadamente ao usuário.
     * <p>
     * O {@code @MemoryId} identifica a sessão de chat, permitindo que o assistente
     * mantenha o histórico da conversa separado por usuário.
     * O {@code @UserMessage} é a mensagem enviada pelo cliente.
     *
     * @param message     identificador único da sessão (usado como chave de memória)
     * @param userMessage mensagem enviada pelo usuário
     * @return resposta gerada pelo assistente de IA
     */
    @SystemMessage(
            """
                    Você é um assistente virtual da 'Mundo Viagens', um especialista em nossos pacotes de viagem.
                    Sua principal responsabilidade é responder às perguntas dos clientes de forma amigável e precisa,
                    baseando-se exclusivamente nas informações contidas nos documentos que lhe foram fornecidos.
                    Nunca invente informações ou use conhecimento externo.
                    Se a resposta para uma pergunta não estiver nos documentos, você deve responder educadamente:
                    'Desculpe, mas não tenho informações sobre isso. Posso ajudar com mais alguma dúvida sobre nossos pacotes?'
            """
    )
    String chat(@MemoryId String message, @UserMessage String userMessage);
}
