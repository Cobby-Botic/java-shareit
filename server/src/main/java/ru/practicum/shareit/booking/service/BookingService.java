package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;

import java.util.List;

public interface BookingService {

    public BookingDto getBooking(Long bookingId, Long userId);

    List<BookingDto> getBookingsByOwner(Long userId);

    BookingDto addBooking(NewBookingDto bookingDto, Long userId);

    BookingDto updateBooking(Long bookingId, Long userId, Boolean approved);

    public List<BookingDto> getBookingsByUser(Long userId);
}
