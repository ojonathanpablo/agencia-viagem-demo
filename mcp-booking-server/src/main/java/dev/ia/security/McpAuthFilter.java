package dev.ia.security;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Protege os endpoints MCP (/mcp/**) com um shared secret entre serviços.
 *
 * Roda na camada Vert.x (antes de JAX-RS e antes do handler do MCP SSE),
 * único ponto que intercepta rotas não-JAX-RS como /mcp/sse e /mcp/messages.
 *
 * Regras:
 *   - /connect/** e /q/** são liberados (OAuth callback e health check)
 *   - Se APP_MCP_SECRET não estiver configurado → bypass total (dev local)
 *   - Comparação em tempo constante para prevenir timing attacks
 *
 * Prioridade 100: roda antes de todos os handlers de aplicação,
 * depois dos handlers de infraestrutura do Quarkus (CORS, etc.).
 */
@ApplicationScoped
public class McpAuthFilter {

    private static final Logger LOG = Logger.getLogger(McpAuthFilter.class);
    private static final String HEADER_NAME = "X-MCP-Secret";

    @ConfigProperty(name = "app.mcp.secret")
    Optional<String> configuredSecret;

    @RouteFilter(100)
    void authenticate(RoutingContext rc) {
        String path = rc.request().path();

        if (!path.startsWith("/mcp/")) {
            rc.next();
            return;
        }

        if (configuredSecret.isEmpty() || configuredSecret.get().isBlank()) {
            LOG.debug("APP_MCP_SECRET não configurado — bypass de auth ativo (dev mode)");
            rc.next();
            return;
        }

        String incoming = rc.request().getHeader(HEADER_NAME);
        String expected = configuredSecret.get();

        if (isValid(incoming, expected)) {
            rc.next();
        } else {
            LOG.warnf("Requisição rejeitada para %s — header %s ausente ou inválido", path, HEADER_NAME);
            rc.response()
              .setStatusCode(401)
              .putHeader("Content-Type", "application/json")
              .end("{\"error\":\"Unauthorized\",\"message\":\"Header X-MCP-Secret ausente ou inválido.\"}");
        }
    }

    /**
     * Comparação segura em tempo constante.
     * Usa SHA-256 em ambos os lados antes do isEqual — garante arrays de mesmo
     * tamanho (32 bytes) independente do comprimento do secret, eliminando o
     * vazamento de comprimento via timing que ocorreria na comparação direta.
     */
    private boolean isValid(String incoming, String expected) {
        if (incoming == null) return false;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] incomingHash = digest.digest(incoming.getBytes(StandardCharsets.UTF_8));
            digest.reset();
            byte[] expectedHash = digest.digest(expected.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(expectedHash, incomingHash);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("SHA-256 não disponível — rejeitando requisição por segurança", e);
            return false;
        }
    }
}