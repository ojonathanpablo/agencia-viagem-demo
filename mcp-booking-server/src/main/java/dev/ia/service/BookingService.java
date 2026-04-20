package dev.ia.service;

import dev.ia.model.Booking;
import dev.ia.model.BookingStatus;
import dev.ia.model.Category;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serviço responsável pela lógica de negócio de reservas de viagem.
 * <p>
 * Utiliza um {@link HashMap} em memória como repositório de dados — os dados são
 * perdidos ao reiniciar a aplicação. Três reservas de exemplo são pré-carregadas
 * no construtor para facilitar testes e demonstrações.
 */
@ApplicationScoped
public class BookingService {

    private final Map<Long, Booking> bookings = new HashMap<>();

    /** Sequência para geração de IDs únicos, iniciando em 100000. */
    private final AtomicLong idSequence = new AtomicLong(100000L);

    /**
     * Inicializa o serviço com três reservas de exemplo para fins de demonstração:
     * <ul>
     *   <li>ID 12345 — John Doe, Tesouros do Egito (TREASURES)</li>
     *   <li>ID 67890 — Jane Smith, Aventura Amazônia (ADVENTURE)</li>
     *   <li>ID 98765 — Peter Jones, Trilha Inca (ADVENTURE)</li>
     * </ul>
     */
    public BookingService() {
        bookings.put(12345L, new Booking(12345L, "John Doe", "Tesouros do Egito",
                LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(2).plusDays(10), BookingStatus.CONFIRMED, Category.TREASURES));
        bookings.put(67890L, new Booking(67890L, "Jane Smith", "Aventura Amazônia",
                LocalDate.now().plusMonths(3), LocalDate.now().plusMonths(3).plusDays(7), BookingStatus.CONFIRMED, Category.ADVENTURE));
        bookings.put(98765L, new Booking(98765L, "Peter Jones", "Trilha Inca",
                LocalDate.now().plusMonths(4), LocalDate.now().plusMonths(4).plusDays(8), BookingStatus.CONFIRMED, Category.ADVENTURE));
    }

    /**
     * Retorna todas as reservas associadas ao nome do usuário informado.
     *
     * @param userName nome do cliente (correspondência exata)
     * @return lista de reservas do usuário, ou lista vazia se não houver nenhuma
     */
    public List<Booking> findBookingsByUser(String userName) {
        return bookings.values().stream()
                .filter(booking -> booking.customerName().equals(userName))
                .toList();
    }

    /**
     * Busca uma reserva pelo seu ID.
     *
     * @param id ID numérico da reserva
     * @return {@link Optional} com a reserva encontrada, ou vazio se não existir
     */
    public Optional<Booking> findBookingDetails(Long id) {
        return Optional.ofNullable(bookings.get(id));
    }

    /**
     * Retorna todas as reservas que pertencem a uma determinada categoria.
     *
     * @param category categoria de filtro ({@code ADVENTURE} ou {@code TREASURES})
     * @return lista de reservas na categoria informada
     */
    public List<Booking> findPackagesByCategory(Category category) {
        return bookings.values().stream()
                .filter(booking -> category.equals(booking.category()))
                .toList();
    }

    /**
     * Cria uma nova reserva com status {@code CONFIRMED} e ID gerado automaticamente.
     *
     * @param customerName nome do cliente
     * @param destination  destino da viagem
     * @param startDate    data de início
     * @param endDate      data de fim
     * @param category     categoria do pacote
     * @return a reserva recém-criada
     */
    public Booking createBooking(String customerName, String destination, LocalDate startDate, LocalDate endDate, Category category) {
        long id = idSequence.incrementAndGet();
        Booking booking = new Booking(id, customerName, destination, startDate, endDate, BookingStatus.CONFIRMED, category);
        bookings.put(id, booking);
        return booking;
    }

    /**
     * Cancela uma reserva existente, validando que o solicitante é o titular.
     * <p>
     * Como {@link Booking} é um record imutável, o cancelamento cria um novo objeto
     * com status {@code CANCELLED} e substitui o registro no mapa.
     * Se o {@code userName} não corresponder ao {@code customerName} da reserva,
     * o cancelamento é recusado e retorna {@link Optional#empty()}.
     *
     * @param bookingId ID da reserva a cancelar
     * @param userName  nome do usuário que solicita o cancelamento
     * @return {@link Optional} com a reserva cancelada, ou vazio se não autorizado/não encontrada
     */
    public Optional<Booking> cancelBooking(long bookingId, String userName) {
        if (bookings.containsKey(bookingId)) {
            Booking booking = bookings.get(bookingId);

            if (booking.customerName().equals(userName)) {
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

