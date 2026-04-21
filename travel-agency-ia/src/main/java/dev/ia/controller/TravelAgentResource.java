package dev.ia.controller;

import dev.ia.dto.TravelPackageList;
import dev.ia.service.TravelAgentService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 * Endpoint REST que expõe o assistente virtual da agência de viagens.
 * <p>
 * Recebe mensagens dos clientes via HTTP e delega toda a lógica de negócio
 * ao {@link TravelAgentService}, mantendo o controller focado apenas no protocolo HTTP.
 */
@Path("/travel")
public class TravelAgentResource {

    @Inject
    TravelAgentService travelAgentService;

    /**
     * Recebe a pergunta do usuário e retorna a resposta do assistente de IA.
     *
     * @param question  pergunta ou instrução enviada pelo cliente no corpo da requisição
     * @param userName  nome do usuário autenticado, obtido do cabeçalho {@code X-User-Name}
     * @return          resposta gerada pelo assistente, ou mensagem de erro se não autenticado
     */
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String ask(String question, @HeaderParam("X-User-Name") String userName) {
        return travelAgentService.chat(question, userName);
    }

    /**
     * Versão streaming do chat — retorna tokens em tempo real via SSE.
     * O último evento inclui o total de tokens consumidos na requisição.
     *
     * @param question  pergunta enviada pelo cliente
     * @param userName  nome do usuário autenticado
     * @return stream de tokens + evento final com uso de tokens
     */
    @POST
    @Blocking
    @Path("/stream")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<String> stream(String question, @HeaderParam("X-User-Name") String userName) {
        return travelAgentService.chatStream(question, userName);
    }

    /**
     * Retorna os pacotes de viagem disponíveis para uma categoria em formato JSON.
     *
     * @param category categoria dos pacotes (ex: ADVENTURE, TREASURES)
     * @return lista de pacotes com a categoria informada
     */
    @GET
    @Blocking
    @Path("/packages/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    public TravelPackageList listPackages(@PathParam("category") String category) {
        return travelAgentService.listPackages(category);
    }

}
