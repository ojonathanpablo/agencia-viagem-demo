package dev.ia.tools;

import dev.ia.service.GoogleCalendarService;
import dev.ia.service.GoogleTokenService;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Ferramentas MCP para integração com o Google Calendar.
 * Segue o mesmo padrão de BookingTools: thin tool layer, lógica no service.
 */
@ApplicationScoped
public class GoogleCalendarTools {

    @Inject
    GoogleTokenService googleTokenService;

    @Inject
    GoogleCalendarService googleCalendarService;

    @ConfigProperty(name = "app.base-url", defaultValue = "http://localhost:8081")
    String baseUrl;

    @Tool(name = "checkGoogleConnection", description = """
            Use esta ferramenta para verificar se o usuario ja conectou a conta Google para acessar o Calendar.
            Quando usar: SEMPRE antes de qualquer operacao no Google Calendar (listar, criar, deletar eventos).
            Entrada: o ID/nome do usuario autenticado na conversa.
            Saida: se CONECTADO, pode prosseguir. Se NAO_CONECTADO, retorna o link de autorizacao.
            """)
    public String checkGoogleConnection(
            @ToolArg(description = "ID ou nome do usuario autenticado na conversa. Exemplo: joao") String userId) {

        if (googleTokenService.isConnected(userId)) {
            return "CONECTADO: usuario autorizado. Pode executar operacoes no Calendar.";
        }

        String authLink = baseUrl + "/connect/google?userId="
                + URLEncoder.encode(userId, StandardCharsets.UTF_8);

        return "NAO_CONECTADO: o usuario ainda nao autorizou o acesso ao Google Calendar. "
                + "Informe de forma amigavel que ele precisa conectar a conta Google. "
                + "Link de autorizacao: " + authLink + " "
                + "Apos conectar, ele pode repetir o pedido normalmente.";
    }

    @Tool(name = "listCalendarEvents", description = """
            Use esta ferramenta para listar os eventos do Google Calendar do usuario em um periodo.
            Quando usar: o usuario quer saber o que tem na agenda, listar compromissos ou ver eventos.
            Chame checkGoogleConnection primeiro se nao tiver certeza que o usuario esta conectado.
            Datas devem estar no formato ISO 8601 com timezone. Fuso horario: America/Sao_Paulo.
            Entrada: userId, data/hora minima e maxima do periodo.
            Saida: lista de eventos com titulo, inicio e fim, ou mensagem de periodo vazio.
            """)
    public String listCalendarEvents(
            @ToolArg(description = "ID ou nome do usuario autenticado") String userId,
            @ToolArg(description = "Data/hora minima no formato ISO 8601 com timezone. Exemplo: 2024-01-15T00:00:00-03:00") String timeMin,
            @ToolArg(description = "Data/hora maxima no formato ISO 8601 com timezone. Exemplo: 2024-01-16T23:59:59-03:00") String timeMax) {

        return googleCalendarService.listEvents(userId, timeMin, timeMax);
    }

    @Tool(name = "createCalendarEvent", description = """
            Use esta ferramenta para criar um evento no Google Calendar do usuario.
            Quando usar: o usuario quer agendar, marcar ou criar um compromisso na agenda.
            Chame checkGoogleConnection primeiro se nao tiver certeza que o usuario esta conectado.
            IMPORTANTE: confirme titulo, data e hora com o usuario antes de criar.
            Datas no formato ISO 8601. Fuso horario: America/Sao_Paulo.
            Saida: ID do evento criado e link para abrir no Google Calendar.
            """)
    public String createCalendarEvent(
            @ToolArg(description = "ID ou nome do usuario autenticado") String userId,
            @ToolArg(description = "Titulo do evento. Exemplo: Reuniao com Maria") String summary,
            @ToolArg(description = "Data e hora de inicio no formato ISO 8601. Exemplo: 2024-01-15T15:00:00") String startDateTime,
            @ToolArg(description = "Data e hora de fim no formato ISO 8601. Exemplo: 2024-01-15T16:00:00") String endDateTime,
            @ToolArg(description = "Descricao opcional do evento. Pode ser string vazia.") String description) {

        return googleCalendarService.createEvent(userId, summary, startDateTime, endDateTime, description);
    }

    @Tool(name = "deleteCalendarEvent", description = """
            Use esta ferramenta para deletar um evento do Google Calendar do usuario.
            Quando usar: o usuario quer cancelar ou remover um compromisso da agenda.
            IMPORTANTE: confirme com o usuario antes de deletar. Use listCalendarEvents para obter o ID.
            Entrada: userId e o ID do evento (obtido via listCalendarEvents).
            Saida: confirmacao de exclusao ou mensagem de erro.
            """)
    public String deleteCalendarEvent(
            @ToolArg(description = "ID ou nome do usuario autenticado") String userId,
            @ToolArg(description = "ID do evento no Google Calendar, obtido via listCalendarEvents") String eventId) {

        return googleCalendarService.deleteEvent(userId, eventId);
    }
}
