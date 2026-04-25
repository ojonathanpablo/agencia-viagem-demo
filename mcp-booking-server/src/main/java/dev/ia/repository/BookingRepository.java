package dev.ia.repository;

import dev.ia.model.Booking;
import dev.ia.model.Category;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class BookingRepository implements PanacheRepository<Booking> {

    public List<Booking> findByCustomerName(String customerName) {
        return list("customerName", customerName);
    }

    public List<Booking> findByCategory(Category category) {
        return list("category", category);
    }
}