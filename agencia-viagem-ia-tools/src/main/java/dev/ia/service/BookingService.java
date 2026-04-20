package dev.ia.service;

import dev.ia.security.SecurityContext;
import dev.ia.model.Booking;
import dev.ia.model.BookingStatus;
import dev.ia.model.Category;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class BookingService {

    private static final Logger LOG = Logger.getLogger(BookingService.class.getName());

    private final Map<Long, Booking> bookings = new HashMap<>();
    private final AtomicLong idSequence = new AtomicLong(100000L);

    public BookingService() {
        bookings.put(12345L, new Booking(12345L, "John Doe", "Tesouros do Egito",
                LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(2).plusDays(10), BookingStatus.CONFIRMED, Category.TREASURES));
        bookings.put(67890L, new Booking(67890L, "Jane Smith", "Aventura Amazônia",
                LocalDate.now().plusMonths(3), LocalDate.now().plusMonths(3).plusDays(7), BookingStatus.CONFIRMED, Category.ADVENTURE));
        bookings.put(98765L, new Booking(98765L, "Peter Jones", "Trilha Inca",
                LocalDate.now().plusMonths(4), LocalDate.now().plusMonths(4).plusDays(8), BookingStatus.CONFIRMED, Category.ADVENTURE));
    }

    public List<Booking> findPackagesByCategory(Category category) {
        return bookings.values().stream()
                .filter(booking -> category.equals(booking.category()))
                .toList();
    }

    public Optional<Booking> getBookingDetails(Long id) {
        return Optional.ofNullable(bookings.get(id));
    }

    public Booking createBooking(String customerName, String destination, LocalDate startDate, LocalDate endDate, Category category) {
        long id = idSequence.incrementAndGet();
        Booking booking = new Booking(id, customerName, destination, startDate, endDate, BookingStatus.CONFIRMED, category);
        bookings.put(id, booking);
        LOG.info("Reserva criada: ID=%d | Cliente=%s | Destino=%s | %s até %s".formatted(id, customerName, destination, startDate, endDate));

        return booking;
    }

    public Optional<Booking> cancelBooking(long bookingId) {
        String currentUser = SecurityContext.getCurrentUser();
        if (bookings.containsKey(bookingId)) {
            Booking booking = bookings.get(bookingId);
            // Validando o usuário "logado", e não apenas o informado
            if (booking.customerName().equals(currentUser)) {
                Booking cancelledBooking = new Booking(
                        booking.id(),
                        booking.customerName(),
                        booking.destination(),
                        booking.startDate(),
                        booking.endDate(),
                        BookingStatus.CANCELLED,
                        booking.category()
                );
                this.bookings.replace(bookingId, cancelledBooking);
                return Optional.of(cancelledBooking);
            }
        }
        return Optional.empty();
    }
}

