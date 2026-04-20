package dev.ia.config;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.mcp.runtime.McpToolBox;

/**
 * Serviço de IA responsável por atender os clientes da agência 'Mundo Viagens'.
 * <p>
 * Registrado como um AI Service do Quarkus LangChain4j via {@link RegisterAiService},
 * com acesso às ferramentas de reserva definidas em {}.
 * O pipeline RAG configurado em {@code RagConfiguration} é aplicado automaticamente,
 * enriquecendo cada pergunta com contexto extraído dos documentos indexados.
 */
@RegisterAiService
public interface PackageExpertWithTemplate  {

    /**
     * Processa a mensagem do usuário e retorna a resposta do assistente.
     * <p>
     * O pipeline completo executado a cada chamada:
     * <ol>
     *   <li>O {@code @MemoryId} (memoryId) identifica a sessão — o histórico da conversa
     *       é carregado do {@code ChatMemory} e enviado ao LLM junto com a nova mensagem.</li>
     *   <li>O pipeline RAG converte a {@code message} em embedding e busca trechos relevantes
     *       dos documentos indexados, injetando-os como contexto adicional.</li>
     *   <li>O {@code @McpToolBox("booking-server")} disponibiliza ao LLM as ferramentas do
     *       servidor MCP (criar/cancelar reserva, listar pacotes etc.), que ele pode invocar
     *       automaticamente conforme necessário.</li>
     *   <li>O {@code @UserMessage} monta o prompt final com a mensagem e o nome do usuário
     *       autenticado, garantindo que as ferramentas usem a identidade correta.</li>
     * </ol>
     *
     * @param memoryId  identificador único da sessão, usado para isolar o histórico de cada usuário
     * @param message   mensagem enviada pelo usuário
     * @param username  nome do usuário autenticado, passado ao LLM para personalização e autorização
     * @return          resposta gerada pelo modelo após consultar RAG e/ou ferramentas MCP
     */
    @SystemMessage("""
        Você é um assistente virtual da 'Mundo Viagens', um especialista em nossos pacotes de viagem e reservas.
        Sua principal responsabilidade é responder às perguntas dos clientes de forma amigável e precisa,
        baseando-se exclusivamente nas informações contidas nos documentos que lhe foram fornecidos (RAG)
        ou utilizando as ferramentas disponíveis para interagir com o sistema de reservas.
        Nunca invente informações ou use conhecimento externo.
        Se a resposta para uma pergunta não estiver nos documentos e nenhuma ferramenta puder ajudar,
        você deve responder educadamente:
        'Desculpe, mas não tenho informações sobre isso. Posso ajudar com mais alguma dúvida sobre nossos pacotes?'
        """)
    @McpToolBox("booking-server")
    @UserMessage("Do what user is asking {message}. The user used for authentication is {username}.")
    String chat(@MemoryId String memoryId, String message, String username);
}
