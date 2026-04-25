package dev.ia.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    public Long id;
    @Column(name = "customer_name")
    public String customerName;
    public String destination;
    @Column(name = "start_date")
    public LocalDate startDate;
    @Column(name = "end_date")
    public LocalDate endDate;
    @Enumerated(EnumType.STRING)
    public BookingStatus status;
    @Enumerated(EnumType.STRING)
    public Category category;

    public Booking(Long id, String customerName, String destination,
                   LocalDate startDate, LocalDate endDate,
                   BookingStatus status, Category category) {
        this.id = id;
        this.customerName = customerName;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.category = category;
    }
    public Booking() {
    }

    @Override
    public String toString() {
        return "ID: " + id +
                " | Cliente: " + customerName +
                " | Destino: " + destination +
                " | Período: " + startDate + " até " + endDate +
                " | Status: " + status +
                " | Categoria: " + category;
    }
}