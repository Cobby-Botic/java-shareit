package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long itemID;
    private Long userId;
    private LocalDate dateFrom;
    private LocalDate dateT0;
}
