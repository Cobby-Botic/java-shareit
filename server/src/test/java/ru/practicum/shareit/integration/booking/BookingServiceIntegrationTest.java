package ru.practicum.shareit.integration.booking;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EntityManager entityManager;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.ru");
        entityManager.persist(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@test.ru");
        entityManager.persist(booker);

        item = new Item();
        item.setName("Дрель");
        item.setDescription("Обычная дрель");
        item.setOwner(owner.getId());
        item.setAvailable(true);
        entityManager.persist(item);

        entityManager.flush();
    }

    @Test
    void addBookingShouldSaveBookingToDatabase() {
        NewBookingDto newBookingDto = new NewBookingDto();
        newBookingDto.setItemId(item.getId());
        newBookingDto.setStart(LocalDateTime.now().plusDays(1));
        newBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto result = bookingService.addBooking(
                newBookingDto,
                booker.getId()
        );

        assertNotNull(result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        assertEquals(booker.getId(), result.getBooker().getId());
        assertEquals(item.getId(), result.getItem().getId());
    }

    @Test
    void getBookingShouldReturnBookingFromDatabase() {
        Booking booking = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        BookingDto result = bookingService.getBooking(
                booking.getId(),
                booker.getId()
        );

        assertEquals(booking.getId(), result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        assertEquals(booker.getId(), result.getBooker().getId());
        assertEquals(item.getId(), result.getItem().getId());
    }

    @Test
    void getBookingsByUserShouldReturnUserBookings() {
        Booking firstBooking = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        Booking secondBooking = createBooking(
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4),
                BookingStatus.APPROVED
        );

        List<BookingDto> result =
                bookingService.getBookingsByUser(booker.getId());

        assertEquals(2, result.size());

        assertTrue(result.stream()
                .anyMatch(booking ->
                        booking.getId().equals(firstBooking.getId())));

        assertTrue(result.stream()
                .anyMatch(booking ->
                        booking.getId().equals(secondBooking.getId())));
    }

    @Test
    void getBookingsByOwnerShouldReturnBookingsForOwnerItems() {
        Booking booking = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        List<BookingDto> result =
                bookingService.getBookingsByOwner(owner.getId());

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
        assertEquals(item.getId(), result.get(0).getItem().getId());
    }

    @Test
    void updateBookingShouldApproveBookingAndSaveStatus() {
        Booking booking = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        BookingDto result = bookingService.updateBooking(
                booking.getId(),
                owner.getId(),
                true
        );

        assertEquals(BookingStatus.APPROVED, result.getStatus());

        entityManager.flush();
        entityManager.clear();

        Booking updatedBooking =
                entityManager.find(Booking.class, booking.getId());

        assertEquals(
                BookingStatus.APPROVED,
                updatedBooking.getStatus()
        );
    }

    private Booking createBooking(
            LocalDateTime start,
            LocalDateTime end,
            BookingStatus status
    ) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(status);

        entityManager.persist(booking);
        entityManager.flush();

        return booking;
    }
}