package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.service.booking.BookingService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto addBooking(@Valid @RequestBody NewBookingDto bookingDto,
                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на добавление новой брони");
        return bookingService.addBooking(bookingDto, userId);
    }

    @GetMapping
    public List<BookingDto> getBookingsByUser(
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        return bookingService.getBookingsByUser(userId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(
            @PathVariable Long bookingId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        return bookingService.getBooking(bookingId, userId);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        return bookingService.getBookingsByOwner(userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateBooking(
            @PathVariable Long bookingId,
            @RequestParam Boolean approved,
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        return bookingService.updateBooking(bookingId, userId, approved);
    }
}
