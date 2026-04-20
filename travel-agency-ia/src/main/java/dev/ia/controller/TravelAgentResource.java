package dev.ia.controller;


import dev.ia.assistant.TravelAssistant;
import dev.ia.dto.ChatResponse;
import dev.langchain4j.guardrail.InputGuardrailException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 * Endpoint REST que expõe o assistente virtual da agência de viagens.
 * <p>
 * Recebe mensagens dos clientes via HTTP e as repassa ao {@link TravelAssistant},
 * que combina RAG e ferramentas MCP para gerar respostas contextualizadas.
 */
@Path("/travel")
public class TravelAgentResource {

    @Inject
    TravelAssistant packageExpert;

    /**
     * Recebe a pergunta do usuário e retorna a resposta do assistente de IA.
     * <p>
     * O cabeçalho {@code X-User-Name} é utilizado como identificador de sessão
     * (memória de conversa) e também é passado ao LLM para personalização das respostas
     * e controle de autorização nas ferramentas de reserva.
     *
     * @param question  pergunta ou instrução enviada pelo cliente no corpo da requisição
     * @param userName  nome do usuário autenticado, obtido do cabeçalho {@code X-User-Name}
     * @return          {@link ChatResponse} com o nome do usuário e a resposta gerada pelo assistente
     */
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public ChatResponse ask(String question, @HeaderParam("X-User-Name") String userName) {
        if (userName != null && !userName.isEmpty()) {
            try {
                String message = packageExpert.chat(userName, question, userName);
                return new ChatResponse(userName, message);
            } catch (InputGuardrailException e) {
                return new ChatResponse(userName, "Desculpe, não consigo processar essa mensagem. Por favor, reformule sua pergunta.");
            }
        } else {
            return new ChatResponse(null, "Usuário precisa estar autenticado!");
        }
    }

}
