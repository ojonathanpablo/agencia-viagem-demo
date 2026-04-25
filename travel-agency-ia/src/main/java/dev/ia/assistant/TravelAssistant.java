package dev.ia.assistant;

import dev.ia.config.guard.InjectionGuard;
import dev.ia.config.guard.JsonStructureGuard;
import dev.ia.config.guard.ToneGuardrail;
import dev.ia.dto.TravelPackageList;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.guardrail.OutputGuardrails;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.mcp.runtime.McpToolBox;
import io.quarkiverse.langchain4j.runtime.aiservice.ChatEvent;
import io.smallrye.mutiny.Multi;

/**
 * Serviço de IA responsável por atender os clientes da agência 'Mundo Viagens'.
 * <p>
 * Registrado como um AI Service do Quarkus LangChain4j via {@link RegisterAiService},
 * com acesso às ferramentas de reserva definidas em {}.
 * O pipeline RAG configurado em {@code RagConfiguration} é aplicado automaticamente,
 * enriquecendo cada pergunta com contexto extraído dos documentos indexados.
 */
@RegisterAiService
public interface TravelAssistant {

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
    @UserMessage("Mensagem do cliente: {message}. Cliente autenticado: {username}.")
    @InputGuardrails(InjectionGuard.class)
    @OutputGuardrails(ToneGuardrail.class)
    String chat(@MemoryId String memoryId, String message, String username);

    /**
     * Lista os pacotes de viagem disponíveis para uma categoria e retorna a resposta em JSON válido.
     * <p>
     * O {@link JsonStructureGuard} intercepta a resposta do LLM e garante que seja um JSON válido.
     * Se o LLM retornar texto com markdown ou formato inválido, ele é instruído a reprocessar
     * até gerar um JSON correto.
     * <p>
     * Exemplo de retorno esperado:
     * <pre>
     * {
     *   "categoria": "ADVENTURE",
     *   "pacotes": [
     *     { "destino": "Aventura Amazônia", "duracao": "7 dias" },
     *     { "destino": "Trilha Inca", "duracao": "8 dias" }
     *   ]
     * }
     * </pre>
     *
     * @param category categoria dos pacotes a listar (ex: ADVENTURE, TREASURES)
     * @return string contendo JSON válido com os pacotes da categoria informada
     */
    @McpToolBox("booking-server")
    @UserMessage("Liste os pacotes disponíveis para a categoria {category}. O campo 'categoria' do resultado deve ser '{category}'.")
    TravelPackageList listPackagesAsJson(String category);

    /**
     * Versão streaming do chat — emite tokens da resposta um a um via SSE.
     * <p>
     * Retorna {@code Multi<ChatEvent>} permitindo ao cliente receber:
     * <ul>
     *   <li>{@code ChatEvent.PartialResponse} — cada token gerado pelo LLM</li>
     *   <li>{@code ChatEvent.Completed} — resposta completa com uso de tokens</li>
     * </ul>
     * Guardrails de saída não são aplicados neste método pois a resposta
     * é emitida token a token antes de estar completa.
     *
     * @param memoryId  identificador da sessão para isolamento de memória
     * @param message   mensagem enviada pelo usuário
     * @param username  nome do usuário autenticado
     * @return stream de eventos da resposta
     */
    @SystemMessage("""
        Você é um assistente virtual da 'Mundo Viagens', um especialista em nossos pacotes de viagem e reservas.
        Sua principal responsabilidade é responder às perguntas dos clientes de forma amigável e precisa,
        baseando-se exclusivamente nas informações contidas nos documentos que lhe foram fornecidos (RAG)
        ou utilizando as ferramentas disponíveis para interagir com o sistema de reservas.
        Nunca invente informações ou use conhecimento externo.
        """)
    @McpToolBox("booking-server")
    @UserMessage("Mensagem do cliente: {message}. Cliente autenticado: {username}.")
    @InputGuardrails(InjectionGuard.class)
    Multi<ChatEvent> chatStream(@MemoryId String memoryId, String message, String username);
}
