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

    @Tool(name = "Obtém os detalhes completos de uma reserva com base em seu número de identificação (bookingId).")
    public String getBookingDetails(
            @ToolArg(description = "O ID númerico único da reserva (ex: 12345)") long bookingId) {

        return bookingService.findBookingDetails(bookingId)
                .map(Booking::toString)
                .orElse("Reserva com ID " + bookingId + " não encontrada.");
    }

    @Tool(name = """
                Cancela uma reserva existente com base no seu ID (bookingId).
                O usuário deve estar autenticado.
            """)
    public String cancelBooking(
            @ToolArg(description = "ID da reserva a cancelar") long bookingId,
            @ToolArg(description = "Usuário que está tentando cancelar a reserva") String name) {

        return bookingService.cancelBooking(bookingId, name)
                .map(b -> "Reserva " + b.id() + " cancelada com sucesso.")
                .orElse("Não foi possível cancelar a reserva. Verifique se o ID está correto e se você tem permissão.");
    }

    @Tool(name = """
                Cria uma nova reserva de viagem para o usuário autenticado.
                Requer: destino (destination), data de início (startDate no formato YYYY-MM-DD),
                data de fim (endDate no formato YYYY-MM-DD) e categoria (category: ADVENTURE ou TREASURES).
            """)
    public String createBooking(
           @ToolArg(description = "Destino da viagem (ex: Paris, Tokyo, Nova York)") String destination,
           @ToolArg(description = "Data de início da viagem no formato YYYY-MM-DD") LocalDate startDate,
           @ToolArg(description = "Data de fim da viagem no formato YYYY-MM-DD") LocalDate endDate,
           @ToolArg(description = "Categoria do pacote de viagem: ADVENTURE ou TREASURES") Category category,
           @ToolArg(description = "Nome do usuário que está realizando a reserva") String userName) {

        Booking booking = bookingService.createBooking(userName, destination, startDate, endDate, category);
        return "Reserva criada com sucesso! ID: " + booking.id() +
                " | Cliente: " + booking.customerName() +
                " | Destino: " + booking.destination() +
                " | Período: " + booking.startDate() + " até " + booking.endDate() +
                " | Status: " + booking.status();
    }

    @Tool(name = "Lista todas as reservas de viagem de um usuário específico.")
    public String listBookingsByUser(
            @ToolArg(description = "Nome do usuário cujas reservas serão listadas") String userName) {

        List<Booking> userBookings = bookingService.findBookingsByUser(userName);
        if (userBookings.isEmpty()) {
            return "Nenhuma reserva encontrada para o usuário: " + userName;
        }

        return "Reservas de " + userName + ":\n" + userBookings.stream()
                .map(Booking::toString)
                .reduce("", (a, b) -> a + "\n" + b);
    }

    @Tool(name = "Lista os pacotes de viagem disponíveis para uma determinada categoria (ex: ADVENTURE, TREASURES).")
    public String listPackagesByCategory(
            @ToolArg(description = "Categoria utilizada como filtro para pacotes") Category category) {

        List<Booking> packages = bookingService.findPackagesByCategory(category);
        if (packages.isEmpty()) {
            return "Nenhum pacote encontrado para a categoria: " + category;
        }

        return "Pacotes encontrados para a categoria '" + category + "': " + packages.stream()
                .map(Booking::destination)
                .toList();
    }
}
