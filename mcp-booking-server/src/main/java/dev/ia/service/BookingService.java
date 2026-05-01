package dev.ia.service;

import dev.ia.model.Booking;
import dev.ia.model.BookingStatus;
import dev.ia.model.Category;
import dev.ia.repository.BookingRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class BookingService {

    @Inject
    BookingRepository bookingRepository;

    public List<Booking> findBookingsByUser(String userName) {
        return bookingRepository.findByCustomerName(userName);
    }

    public Optional<Booking> findBookingDetails(Long id) {
        return bookingRepository.findByIdOptional(id);
    }

    public List<Booking> findPackagesByCategory(Category category) {
        return bookingRepository.findByCategory(category);
    }

    @Transactional
    public Booking createBooking(String customerName, String destination,
                                 LocalDate startDate, LocalDate endDate, Category category) {

        long id = 100000L + ThreadLocalRandom.current().nextLong(900000L);

        Booking booking = new Booking(id, customerName, destination, startDate, endDate,BookingStatus.CONFIRMED, category);

        bookingRepository.persist(booking);
        return booking;
    }

    @Transactional
    public Optional<Booking> cancelBooking(long bookingId, String userName) {
        Optional<Booking> booking = bookingRepository.findByIdOptional(bookingId);

        if (booking.isEmpty() || !booking.get().customerName.equals(userName)) {
            return Optional.empty();
        }

        Booking found = booking.get();
        found.status = BookingStatus.CANCELLED;
        bookingRepository.persist(found);

        return Optional.of(found);
    }

    public Optional<Long> parseBookingId(String bookingId) {
        try {
            return Optional.of(Long.parseLong(bookingId.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}