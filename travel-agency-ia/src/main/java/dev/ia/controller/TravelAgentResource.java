package dev.ia.controller;


import dev.ia.assistant.TravelAssistant;
import dev.langchain4j.guardrail.InputGuardrailException;
import dev.langchain4j.guardrail.OutputGuardrailException;
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
     * @return          resposta gerada pelo assistente, ou mensagem de erro se não autenticado
     */
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String ask(String question, @HeaderParam("X-User-Name") String userName) {
        if (userName != null && !userName.isEmpty()) {
            try {
                return packageExpert.chat(userName, question, userName);
            } catch (InputGuardrailException e) {
                return "Desculpe, não consigo processar essa mensagem. Por favor, reformule sua pergunta.";
            } catch (OutputGuardrailException e) {
                return "Desculpe, ocorreu um problema ao processar a resposta. Por favor, tente novamente.";
            }
        } else {
            return "Usuário precisa estar autenticado!";
        }
    }

    /**
     * Retorna os pacotes de viagem disponíveis para uma categoria em formato JSON.
     * <p>
     * A resposta é gerada pelo LLM e validada pelo {@code JsonStructureGuard},
     * que garante que o retorno seja sempre um JSON válido.
     *
     * @param category categoria dos pacotes (ADVENTURE ou TREASURES)
     * @return string contendo JSON com os pacotes da categoria informada
     */
    @GET
    @Path("/packages/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    public String listPackages(@PathParam("category") String category) {
        return packageExpert.listPackagesAsJson(category);
    }

}
