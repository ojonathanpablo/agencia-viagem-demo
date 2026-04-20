package dev.ia.tools;

import dev.ia.security.SecurityContext;
import dev.ia.model.Booking;
import dev.ia.model.Category;
import dev.ia.service.BookingService;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class BookingTools {

    @Inject
    BookingService bookingService;

    @Tool("Obtém os detalhes completos de uma reserva com base em seu número de identificação (bookingId).")
    public String getBookingDetails(long bookingId) {
        return bookingService.getBookingDetails(bookingId)
                .map(Booking::toString)
                .orElse("Reserva com ID " + bookingId + " não encontrada.");
    }

    @Tool("""
        Cancela uma reserva existente com base no seu ID (bookingId).
        O usuário deve estar autenticado.
    """)
    public String cancelBooking(long bookingId) {
        return bookingService.cancelBooking(bookingId)
                .map(b -> "Reserva " + b.id() + " cancelada com sucesso.")
                .orElse("Não foi possível cancelar a reserva. Verifique se o ID está correto e se você tem permissão.");
    }

    @Tool("""
        Cria uma nova reserva de viagem para o usuário autenticado.
        Requer: destino (destination), data de início (startDate no formato YYYY-MM-DD),
        data de fim (endDate no formato YYYY-MM-DD) e categoria (category: ADVENTURE ou TREASURES).
    """)
    public String createBooking(String destination, LocalDate startDate, LocalDate endDate, Category category) {
        String customerName = SecurityContext.getCurrentUser();
        Booking booking = bookingService.createBooking(customerName, destination, startDate, endDate, category);
        return "Reserva criada com sucesso! ID: " + booking.id() +
                " | Cliente: " + booking.customerName() +
                " | Destino: " + booking.destination() +
                " | Período: " + booking.startDate() + " até " + booking.endDate() +
                " | Status: " + booking.status();
    }

    @Tool("Lista os pacotes de viagem disponíveis para uma determinada categoria (ex: ADVENTURE, TREASURES).")
    public String listPackagesByCategory(Category category) {
        List<Booking> packages = bookingService.findPackagesByCategory(category);
        if (packages.isEmpty()) {
            return "Nenhum pacote encontrado para a categoria: " + category;
        }
        return "Pacotes encontrados para a categoria '" + category + "': " + packages.stream()
                .map(Booking::destination)
                .toList();
    }
}
