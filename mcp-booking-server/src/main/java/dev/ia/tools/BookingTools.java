package dev.ia.tools;

import dev.ia.model.Booking;
import dev.ia.model.Category;
import dev.ia.service.BookingService;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class BookingTools {

    @Inject
    BookingService bookingService;

    @Tool(name = "getBookingDetails", description = """
            Use esta ferramenta para buscar os detalhes de uma reserva.
            Quando usar: o usuario quer saber informacoes de uma reserva especifica (datas, destino, status).
            Entrada: o numero ID da reserva.
            Saida: detalhes completos da reserva ou mensagem de nao encontrada.
            """)
    public String getBookingDetails(
            @ToolArg(description = "ID numerico da reserva. Exemplo: 12345") long bookingId) {

        return bookingService.findBookingDetails(bookingId)
                .map(Booking::toString)
                .orElse("Reserva com ID " + bookingId + " nao encontrada.");
    }

    @Tool(name = "cancelBooking", description = """
            Use esta ferramenta para cancelar uma reserva existente.
            Quando usar: o usuario quer cancelar uma reserva e informa o ID e o proprio nome.
            Regra importante: o nome informado deve ser exatamente o mesmo nome do titular da reserva.
            Entrada: ID da reserva e nome do usuario.
            Saida: confirmacao de cancelamento ou mensagem de erro se nao autorizado.
            """)
    public String cancelBooking(
            @ToolArg(description = "ID numerico da reserva a cancelar. Exemplo: 12345") long bookingId,
            @ToolArg(description = "Nome completo do usuario que esta solicitando o cancelamento. Deve ser identico ao nome do titular.") String name) {

        return bookingService.cancelBooking(bookingId, name)
                .map(booking -> "Reserva " + booking.id + " cancelada com sucesso.")
                .orElse("Nao foi possivel cancelar a reserva. Verifique se o ID esta correto e se voce e o titular.");
    }

    @Tool(name = "createBooking", description = """
            Use esta ferramenta para criar uma nova reserva de viagem.
            Quando usar: o usuario quer reservar uma viagem e informou destino, datas e categoria.
            Entradas obrigatorias: destino, data de inicio, data de fim, categoria e nome do usuario.
            Categoria deve ser exatamente: ADVENTURE ou TREASURES.
            Datas devem estar no formato: YYYY-MM-DD.
            Saida: resumo da reserva criada com o ID gerado.
            """)
    public String createBooking(
            @ToolArg(description = "Destino da viagem. Exemplo: Paris, Tokyo, Amazonia") String destination,
            @ToolArg(description = "Data de inicio no formato YYYY-MM-DD. Exemplo: 2026-06-01") LocalDate startDate,
            @ToolArg(description = "Data de fim no formato YYYY-MM-DD. Exemplo: 2026-06-10") LocalDate endDate,
            @ToolArg(description = "Categoria do pacote. Valores aceitos: ADVENTURE ou TREASURES") Category category,
            @ToolArg(description = "Nome completo do usuario que esta fazendo a reserva") String userName) {

        Booking booking = bookingService.createBooking(userName, destination, startDate, endDate, category);
        return "Reserva criada com sucesso! ID: " + booking.id +
                " | Cliente: " + booking.customerName +
                " | Destino: " + booking.destination +
                " | Periodo: " + booking.startDate + " ate " + booking.endDate +
                " | Status: " + booking.status;
    }

    @Tool(name = "listBookingsByUser", description = """
            Use esta ferramenta para listar todas as reservas de um usuario.
            Quando usar: o usuario pergunta "quais sao minhas reservas" ou quer ver seu historico de viagens.
            Entrada: nome completo do usuario.
            Saida: lista de todas as reservas do usuario ou mensagem informando que nao ha reservas.
            """)
    public String listBookingsByUser(
            @ToolArg(description = "Nome completo do usuario cujas reservas serao listadas") String userName) {

        List<Booking> bookings = bookingService.findBookingsByUser(userName);
        if (bookings.isEmpty()) {
            return "Nenhuma reserva encontrada para o usuario: " + userName;
        }

        return "Reservas de " + userName + ":\n" + bookings.stream()
                .map(Booking::toString)
                .reduce("", (a, b) -> a + "\n" + b);
    }

    @Tool(name = "listPackagesByCategory", description = """
            Use esta ferramenta para listar pacotes de viagem disponiveis por categoria.
            Quando usar: o usuario quer explorar opcoes de viagem ou pergunta quais pacotes existem.
            Entrada: categoria desejada - ADVENTURE (aventura, natureza) ou TREASURES (cultura, historia).
            Saida: lista de pacotes disponiveis na categoria informada.
            """)
    public String listPackagesByCategory(
            @ToolArg(description = "Categoria do pacote. Valores aceitos: ADVENTURE ou TREASURES") Category category) {

        List<Booking> packages = bookingService.findPackagesByCategory(category);
        if (packages.isEmpty()) {
            return "Nenhum pacote encontrado para a categoria: " + category;
        }

        return "Pacotes disponiveis na categoria '" + category + "':\n" + packages.stream()
                .map(b -> "- Destino: " + b.destination
                        + " | De: " + b.startDate
                        + " ate: " + b.endDate
                        + " | Status: " + b.status)
                .reduce("", (a, b) -> a + "\n" + b);
    }
}