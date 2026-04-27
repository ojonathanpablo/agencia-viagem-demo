INSERT INTO bookings (id, customer_name, destination, start_date, end_date, status, category)
VALUES (12345, 'John Doe', 'Tesouros do Egito', CURRENT_DATE + INTERVAL '2 months', CURRENT_DATE + INTERVAL '2 months 10 days', 'CONFIRMED', 'TREASURES');

INSERT INTO bookings (id, customer_name, destination, start_date, end_date, status, category)
VALUES (67890, 'Jane Smith', 'Aventura Amazonia', CURRENT_DATE + INTERVAL '3 months', CURRENT_DATE + INTERVAL '3 months 7 days', 'CONFIRMED', 'ADVENTURE');

INSERT INTO bookings (id, customer_name, destination, start_date, end_date, status, category)
VALUES (98765, 'Peter Jones', 'Trilha Inca', CURRENT_DATE + INTERVAL '4 months', CURRENT_DATE + INTERVAL '4 months 8 days', 'CONFIRMED', 'ADVENTURE');