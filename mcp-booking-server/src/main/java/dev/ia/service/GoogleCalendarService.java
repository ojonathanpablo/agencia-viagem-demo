package dev.ia.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Regras de negócio para operações no Google Calendar.
 * Realiza as chamadas à API REST do Google e retorna respostas formatadas.
 */
@ApplicationScoped
public class GoogleCalendarService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String CALENDAR_API = "https://www.googleapis.com/calendar/v3";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ERRO_PREFIX = "ERRO: ";
    private static final String ERRO_SUFFIX = ". Peca ao usuario para conectar a conta Google.";

    @Inject
    GoogleTokenService googleTokenService;

    public String listEvents(String userId, String timeMin, String timeMax) {
        try {
            String token = googleTokenService.getValidAccessToken(userId);
            String url = CALENDAR_API + "/calendars/primary/events"
                    + "?timeMin=" + encode(timeMin)
                    + "&timeMax=" + encode(timeMax)
                    + "&singleEvents=true"
                    + "&orderBy=startTime"
                    + "&timeZone=America%2FSao_Paulo";

            String json = get(url, token);

            if (!json.contains("\"summary\"")) {
                return "Nenhum evento encontrado no periodo informado.";
            }
            return "Eventos encontrados:\n" + formatEvents(json);

        } catch (IllegalStateException e) {
            return ERRO_PREFIX + e.getMessage() + ERRO_SUFFIX;
        } catch (Exception e) {
            return "Erro ao listar eventos: " + e.getMessage();
        }
    }

    public String createEvent(String userId, String summary, String startDateTime,
                              String endDateTime, String description) {
        try {
            String token = googleTokenService.getValidAccessToken(userId);
            String body = buildEventJson(summary, description, startDateTime, endDateTime);
            String response = postJson(CALENDAR_API + "/calendars/primary/events", body, token);

            String eventId = extractField(response, "id");
            String htmlLink = extractField(response, "htmlLink");
            return "Evento criado com sucesso! ID: " + eventId + " | Link: " + htmlLink;

        } catch (IllegalStateException e) {
            return ERRO_PREFIX + e.getMessage() + ERRO_SUFFIX;
        } catch (Exception e) {
            return "Erro ao criar evento: " + e.getMessage();
        }
    }

    public String deleteEvent(String userId, String eventId) {
        try {
            String token = googleTokenService.getValidAccessToken(userId);
            String url = CALENDAR_API + "/calendars/primary/events/" + eventId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header(AUTH_HEADER, BEARER_PREFIX + token)
                    .DELETE()
                    .build();

            int status = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).statusCode();
            return status == 204 ? "Evento deletado com sucesso." : "Falha ao deletar. Status: " + status;

        } catch (IllegalStateException e) {
            return ERRO_PREFIX + e.getMessage() + ERRO_SUFFIX;
        } catch (Exception e) {
            return "Erro ao deletar evento: " + e.getMessage();
        }
    }

    private String get(String url, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, BEARER_PREFIX + token)
                .GET()
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    private String postJson(String url, String body, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, BEARER_PREFIX + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    private String buildEventJson(String summary, String description,
                                  String startDateTime, String endDateTime) {
        return "{"
                + "\"summary\":" + jsonString(summary) + ","
                + "\"description\":" + jsonString(description) + ","
                + "\"start\":{\"dateTime\":" + jsonString(startDateTime)
                + ",\"timeZone\":\"America/Sao_Paulo\"},"
                + "\"end\":{\"dateTime\":" + jsonString(endDateTime)
                + ",\"timeZone\":\"America/Sao_Paulo\"}"
                + "}";
    }

    private String formatEvents(String json) {
        StringBuilder result = new StringBuilder();
        int pos = json.indexOf("\"items\"");
        if (pos == -1) return "Nenhum evento.";

        while (true) {
            int summaryIdx = json.indexOf("\"summary\"", pos);
            if (summaryIdx == -1) break;

            String summary = extractFieldAt(json, summaryIdx);
            int startIdx = json.indexOf("\"dateTime\"", summaryIdx);
            String start = startIdx != -1 ? extractFieldAt(json, startIdx) : "?";
            int endIdx = startIdx != -1 ? json.indexOf("\"dateTime\"", startIdx + 1) : -1;
            String end = endIdx != -1 ? extractFieldAt(json, endIdx) : "?";

            result.append("- ").append(summary)
                  .append(" | Inicio: ").append(start)
                  .append(" | Fim: ").append(end)
                  .append("\n");

            pos = (endIdx != -1 ? endIdx : summaryIdx) + 1;
        }
        return result.toString().trim();
    }

    private String extractField(String json, String field) {
        int idx = json.indexOf("\"" + field + "\"");
        if (idx == -1) throw new IllegalArgumentException("Campo '" + field + "' nao encontrado.");
        return extractFieldAt(json, idx);
    }

    private String extractFieldAt(String json, int fieldNameIdx) {
        int start = json.indexOf(':', fieldNameIdx) + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            return json.substring(start + 1, end);
        }
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.')) end++;
        return json.substring(start, end);
    }

    private String jsonString(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
