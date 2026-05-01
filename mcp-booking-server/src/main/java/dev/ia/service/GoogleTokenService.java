package dev.ia.service;

import dev.ia.model.UserToken;
import dev.ia.repository.UserTokenRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Gerencia o ciclo de vida dos tokens OAuth2 do Google por usuário.
 * Responsável por: verificar conexão, construir URL de autorização,
 * trocar código por token e renovar token expirado automaticamente.
 */
@ApplicationScoped
public class GoogleTokenService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String SCOPES =
            "openid email profile " +
            "https://www.googleapis.com/auth/calendar.events " +
            "https://www.googleapis.com/auth/calendar.readonly";

    @ConfigProperty(name = "app.google.client-id", defaultValue = "")
    String clientId;

    @ConfigProperty(name = "app.google.client-secret", defaultValue = "")
    String clientSecret;

    @ConfigProperty(name = "app.google.redirect-uri",
                    defaultValue = "http://localhost:8081/connect/google/callback")
    String redirectUri;

    @Inject
    UserTokenRepository tokenRepository;

    public boolean isConnected(String userId) {
        return tokenRepository.findByIdOptional(userId)
                .map(t -> t.accessToken != null && !t.accessToken.isBlank())
                .orElse(false);
    }

    /**
     * Constrói a URL de autorização do Google.
     * O userId é passado como 'state' para ser recuperado no callback.
     */
    public String buildAuthUrl(String userId) {
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&response_type=code"
                + "&scope=" + encode(SCOPES)
                + "&state=" + encode(userId)
                + "&access_type=offline"
                + "&prompt=consent";
    }

    /**
     * Troca o código de autorização por tokens e salva no banco.
     * Chamado pelo GoogleOAuthResource após o usuário autorizar no Google.
     */
    @Transactional
    public void exchangeCodeAndSave(String code, String userId) throws Exception {
        String body = "code=" + encode(code)
                + "&client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&redirect_uri=" + encode(redirectUri)
                + "&grant_type=authorization_code";

        String json = postForm(TOKEN_ENDPOINT, body);

        UserToken token = tokenRepository.findById(userId);
        if (token == null) {
            token = new UserToken(userId);
        }
        token.accessToken = extractField(json, "access_token");
        token.expiresAt = Instant.now().plusSeconds(Long.parseLong(extractField(json, "expires_in")));

        String refreshToken = extractFieldOptional(json, "refresh_token");
        if (refreshToken != null) {
            token.refreshToken = refreshToken;
        }

        tokenRepository.persist(token);
    }

    /**
     * Retorna um access token válido, renovando automaticamente se expirado.
     */
    @Transactional
    public String getValidAccessToken(String userId) throws Exception {
        UserToken token = tokenRepository.findById(userId);
        if (token == null || token.accessToken == null) {
            throw new IllegalStateException("Token não encontrado para: " + userId);
        }

        boolean expirado = token.expiresAt != null
                && Instant.now().isAfter(token.expiresAt.minusSeconds(60));

        if (expirado) {
            if (token.refreshToken == null) {
                throw new IllegalStateException("Token expirado e sem refresh token para: " + userId);
            }
            String body = "client_id=" + encode(clientId)
                    + "&client_secret=" + encode(clientSecret)
                    + "&refresh_token=" + encode(token.refreshToken)
                    + "&grant_type=refresh_token";

            String json = postForm(TOKEN_ENDPOINT, body);
            token.accessToken = extractField(json, "access_token");
            token.expiresAt = Instant.now().plusSeconds(Long.parseLong(extractField(json, "expires_in")));
        }

        return token.accessToken;
    }

    private String postForm(String url, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * Extrai o valor de um campo de um JSON simples (sem biblioteca).
     * Suporta valores string (entre aspas) e numéricos.
     */
    private String extractField(String json, String field) {
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) throw new IllegalArgumentException("Campo '" + field + "' não encontrado no JSON: " + json);
        int colon = json.indexOf(':', idx + key.length());
        int start = colon + 1;
        while (json.charAt(start) == ' ') start++;
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            return json.substring(start + 1, end);
        }
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.')) end++;
        return json.substring(start, end);
    }

    private String extractFieldOptional(String json, String field) {
        try { return extractField(json, field); } catch (Exception e) { return null; }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
