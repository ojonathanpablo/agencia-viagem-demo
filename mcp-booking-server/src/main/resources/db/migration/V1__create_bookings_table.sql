CREATE TABLE bookings (
    id            BIGINT      PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    destination   VARCHAR(255) NOT NULL,
    start_date    DATE         NOT NULL,
    end_date      DATE         NOT NULL,
    status        VARCHAR(50)  NOT NULL,
    category      VARCHAR(50)  NOT NULL
);