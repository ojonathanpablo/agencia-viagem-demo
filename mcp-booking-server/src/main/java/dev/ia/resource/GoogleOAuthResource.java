package dev.ia.resource;

import dev.ia.service.GoogleTokenService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;

/**
 * Endpoints REST para o fluxo OAuth2 com o Google.
 *
 * Fluxo completo:
 *   1. Agente retorna link: GET /connect/google?userId=joao
 *   2. Usuário abre o link → redirecionado para tela de login do Google
 *   3. Google redireciona para: GET /connect/google/callback?code=...&state=joao
 *   4. Token salvo no banco e usuário volta ao chat
 */
@Path("/connect/google")
public class GoogleOAuthResource {

    @Inject
    GoogleTokenService googleTokenService;

    @ConfigProperty(name = "app.base-url", defaultValue = "http://localhost:8081")
    String baseUrl;

    /**
     * Inicia o fluxo OAuth2 redirecionando para a tela de consentimento do Google.
     *
     * @param userId identificador do usuário (X-User-Name), passado como state no OAuth2
     */
    @GET
    public Response connect(@QueryParam("userId") String userId) {
        if (userId == null || userId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Parâmetro 'userId' é obrigatório.")
                    .build();
        }
        String authUrl = googleTokenService.buildAuthUrl(userId);
        return Response.temporaryRedirect(URI.create(authUrl)).build();
    }

    /**
     * Callback chamado pelo Google após o usuário autorizar o acesso.
     * Troca o código pelo token, salva no banco e redireciona ao chat.
     *
     * @param code  código de autorização gerado pelo Google
     * @param state userId enviado no início do fluxo via parâmetro state
     */
    @GET
    @Path("/callback")
    public Response callback(@QueryParam("code") String code,
                             @QueryParam("state") String state) {
        if (code == null || state == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Parâmetros 'code' e 'state' são obrigatórios.")
                    .build();
        }
        try {
            googleTokenService.exchangeCodeAndSave(code, state);
            return Response.temporaryRedirect(URI.create(baseUrl + "/?connected=true")).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("Falha ao conectar conta Google: " + e.getMessage())
                    .build();
        }
    }
}
