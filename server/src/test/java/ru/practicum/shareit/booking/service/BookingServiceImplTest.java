package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.item.exception.NotOwnerException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    BookingServiceImpl bookingService;

    @Test
    void createNewBookingShouldCreateBooking() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setName("Daniel");
        user.setEmail("daniel@test.ru");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        Long itemId = 20L;

        Item item = new Item();
        item.setId(itemId);
        item.setName("Дрель");
        item.setDescription("Обычная дрель");
        item.setAvailable(true);
        item.setOwner(2L);

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        NewBookingDto bookingDto = new NewBookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.of(2026, 1, 1, 0, 0));
        bookingDto.setEnd(LocalDateTime.of(2026, 1, 2, 0, 0));

        when(bookingRepository.existsByItemIdAndStartBeforeAndEndAfter(
                itemId,
                bookingDto.getEnd(),
                bookingDto.getStart()
        )).thenReturn(false);

        Booking savedBooking = new Booking();
        savedBooking.setId(30L);
        savedBooking.setItem(item);
        savedBooking.setBooker(user);
        savedBooking.setStart(bookingDto.getStart());
        savedBooking.setEnd(bookingDto.getEnd());
        savedBooking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(savedBooking);

        BookingDto result =
                bookingService.addBooking(bookingDto, userId);

        assertEquals(30L, result.getId());
        assertEquals(bookingDto.getStart(), result.getStart());
        assertEquals(bookingDto.getEnd(), result.getEnd());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    @Test
    void createNewBookingShouldThrowWhenUserNotFound() {
        Long userId = 1L;

        NewBookingDto bookingDto = new NewBookingDto();
        bookingDto.setItemId(20L);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.addBooking(bookingDto, userId)
                );

        assertEquals("User с ID: " + userId + " не существует", exception.getMessage());

        verify(itemRepository, never())
                .findById(anyLong());

        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void createNewBookingShouldThrowWhenItemNotFound() {
        Long userId = 1L;
        Long itemId = 10L;

        User user = new User();
        user.setId(userId);
        user.setName("Daniel");
        user.setEmail("daniel@test.ru");

        NewBookingDto bookingDto = new NewBookingDto();
        bookingDto.setItemId(itemId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.addBooking(bookingDto, userId)
        );

        assertEquals(
                "Item с ID: " + itemId + " не существует",
                exception.getMessage()
        );

        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void createNewBookingShouldThrowWhenUserIsOwner() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setName("Daniel");
        user.setEmail("daniel@test.ru");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        Long itemId = 20L;

        Item item = new Item();
        item.setId(itemId);
        item.setName("Дрель");
        item.setDescription("Обычная дрель");
        item.setAvailable(true);
        item.setOwner(userId);

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        NewBookingDto bookingDto = new NewBookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.of(2026, 1, 1, 0, 0));
        bookingDto.setEnd(LocalDateTime.of(2026, 1, 2, 0, 0));

        ValidateException exception = assertThrows(
                ValidateException.class,
                () -> bookingService.addBooking(bookingDto, userId)
        );

        assertEquals("Владелец вещи не может бронировать собственную вещь", exception.getMessage());
        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void createNewBookingShouldThrowWhenItemNotAvailable() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setName("Daniel");
        user.setEmail("daniel@test.ru");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        Long itemId = 20L;

        Item item = new Item();
        item.setId(itemId);
        item.setName("Дрель");
        item.setDescription("Обычная дрель");
        item.setAvailable(false);
        item.setOwner(2L);

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        NewBookingDto bookingDto = new NewBookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.of(2026, 1, 1, 0, 0));
        bookingDto.setEnd(LocalDateTime.of(2026, 1, 2, 0, 0));

        ValidateException exception = assertThrows(
                ValidateException.class,
                () -> bookingService.addBooking(bookingDto, userId)
        );

        assertEquals("Нельзя забронировать недоступную вещь", exception.getMessage());
        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void createNewBookingShouldThrowWhenStartIsAfterEnd() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setName("Daniel");
        user.setEmail("daniel@test.ru");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        Long itemId = 20L;

        Item item = new Item();
        item.setId(itemId);
        item.setName("Дрель");
        item.setDescription("Обычная дрель");
        item.setAvailable(true);
        item.setOwner(2L);

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        NewBookingDto bookingDto = new NewBookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.of(2026, 1, 2, 0, 0));
        bookingDto.setEnd(LocalDateTime.of(2026, 1, 1, 0, 0));

        ValidateException exception = assertThrows(
                ValidateException.class,
                () -> bookingService.addBooking(bookingDto, userId)
        );

        assertEquals("Дата начала должна быть раньше даты окончания", exception.getMessage());
        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void createNewBookingShouldThrowWhenBookingAlreadyExists() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setName("Daniel");
        user.setEmail("daniel@test.ru");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        Long itemId = 20L;

        Item item = new Item();
        item.setId(itemId);
        item.setName("Дрель");
        item.setDescription("Обычная дрель");
        item.setAvailable(true);
        item.setOwner(2L);

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        NewBookingDto bookingDto = new NewBookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.of(2026, 1, 1, 0, 0));
        bookingDto.setEnd(LocalDateTime.of(2026, 1, 2, 0, 0));

        when(bookingRepository.existsByItemIdAndStartBeforeAndEndAfter(
                itemId,
                bookingDto.getEnd(),
                bookingDto.getStart()
        ))
                .thenReturn(true);

        ValidateException exception = assertThrows(
                ValidateException.class,
                () -> bookingService.addBooking(bookingDto, userId)
        );

        assertEquals("На выбранное время вещь уже забронирована", exception.getMessage());
        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void getBookingShouldReturnBooking() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setName("Daniel");
        user.setEmail("daniel@test.ru");

        Long itemId = 10L;

        Item item = new Item();
        item.setId(itemId);
        item.setName("Дрель");
        item.setDescription("Обычная дрель");
        item.setAvailable(true);
        item.setOwner(2L);

        Long bookingId = 20L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStart(LocalDateTime.of(2026, 1, 1, 0, 0));
        booking.setEnd(LocalDateTime.of(2026, 1, 2, 0, 0));
        booking.setStatus(BookingStatus.WAITING);
        booking.setItem(item);
        booking.setBooker(user);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        BookingDto result = bookingService.getBooking(bookingId, userId);

        assertEquals(bookingId, result.getId());

        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
        assertEquals(BookingStatus.WAITING, result.getStatus());

        assertEquals(itemId, result.getItem().getId());
        assertEquals("Дрель", result.getItem().getName());

        assertEquals(userId, result.getBooker().getId());
        assertEquals("Daniel", result.getBooker().getName());
    }

    @Test
    void getBookingShouldThrowWhenBookingNotFound() {
        Long bookingId = 1L;
        Long userId = 2L;

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.getBooking(bookingId, userId)
        );

        assertEquals("Бронь с id: " + bookingId + " не существует", exception.getMessage());

        verify(bookingRepository, never())
                .save(any(Booking.class));
    }

    @Test
    void getBookingShouldThrowWhenUserNotFound() {
        Long bookingId = 1L;
        Long userId = 2L;

        Booking booking = new Booking();
        booking.setId(bookingId);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.getBooking(bookingId, userId)
        );

        assertEquals("User с id: " + userId + " не существует", exception.getMessage());
        verify(bookingRepository, never())
                .save(any(Booking.class));
    }

    @Test
    void getBookingShouldThrowWhenUserNoAccess() {
        Long bookingId = 1L;
        Long userId = 2L;

        Item item = new Item();
        item.setOwner(4L);

        User booker = new User();
        booker.setId(3L);

        User user = new User();
        user.setId(10L);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setBooker(booker);
        booking.setItem(item);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        NotOwnerException exception = assertThrows(
                NotOwnerException.class,
                () -> bookingService.getBooking(bookingId, userId)
                );

        assertEquals("Пользователь не имеет доступа к этому бронированию", exception.getMessage());
        verify(bookingRepository, never())
                .save(any(Booking.class));
    }

    @Test
    void getBookingsByUserShouldReturnBookings() {
        Long userId = 10L;
        User user = new User();
        user.setId(userId);

        Long itemId = 15L;
        Item item = new Item();
        item.setId(itemId);

        Long bookingId = 20L;
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItem(item);
        booking.setBooker(user);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findAllByBookerId(userId))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsByUser(userId);

        assertEquals(1, result.size());
        assertEquals(bookingId, result.get(0).getId());
        assertEquals(itemId, result.get(0).getItem().getId());
        assertEquals(user.getId(), result.get(0).getBooker().getId());
    }

    @Test
    void getBookingsByUserShouldReturnEmptyListBookings() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findAllByBookerId(userId))
                .thenReturn(List.of());

        List<BookingDto> result = bookingService.getBookingsByUser(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getBookingsByUserShouldThrowWhenUserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.getBookingsByUser(userId)
        );

        assertEquals("User с id: " + userId + " не существует", exception.getMessage());
        verify(bookingRepository, never())
                .findAllByBookerId(anyLong());
    }

    @Test
    void getBookingsByOwnerShouldReturnBookings() {
        Long ownerId = 10L;

        User owner = new User();
        owner.setId(ownerId);

        Long itemId = 15L;
        Item item = new Item();
        item.setId(itemId);
        item.setOwner(ownerId);

        User booker = new User();
        booker.setId(20L);

        Long bookingId = 30L;
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItem(item);
        booking.setBooker(booker);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(bookingRepository.findAllByItemOwner(ownerId))
                .thenReturn(List.of(booking));

        List<BookingDto> result =
                bookingService.getBookingsByOwner(ownerId);

        assertEquals(1, result.size());
        assertEquals(bookingId, result.get(0).getId());
        assertEquals(itemId, result.get(0).getItem().getId());
        assertEquals(booker.getId(), result.get(0).getBooker().getId());
    }

    @Test
    void getBookingsByOwnerShouldReturnEmptyList() {
        Long ownerId = 1L;

        User owner = new User();
        owner.setId(ownerId);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(bookingRepository.findAllByItemOwner(ownerId))
                .thenReturn(List.of());

        List<BookingDto> result =
                bookingService.getBookingsByOwner(ownerId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getBookingsByOwnerShouldThrowWhenUserNotFound() {
        Long ownerId = 1L;

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.getBookingsByOwner(ownerId)
        );

        assertEquals(
                "User с id: " + ownerId + " не существует",
                exception.getMessage()
        );

        verify(bookingRepository, never())
                .findAllByItemOwner(anyLong());
    }

    @Test
    void updateBookingShouldApproveBooking() {
        Long userId = 5L;
        User user = new User();
        user.setId(userId);

        Long bookerid = 4l;
        User booker = new User();
        booker.setId(bookerid);

        Long itemId = 6L;
        Item item = new Item();
        item.setId(itemId);
        item.setOwner(userId);

        Long bookingId = 10L;
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingDto result = bookingService.updateBooking(bookingId, userId, true);

        assertEquals(bookingId, result.getId());
        assertEquals(BookingStatus.APPROVED, result.getStatus());
        verify(bookingRepository, times(1))
                .save(any(Booking.class));
    }

    @Test
    void updateBookingShouldRejectBooking() {
        Long userId = 5L;

        User user = new User();
        user.setId(userId);

        User booker = new User();
        booker.setId(4L);

        Item item = new Item();
        item.setId(6L);
        item.setOwner(userId);

        Long bookingId = 10L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingDto result =
                bookingService.updateBooking(bookingId, userId, false);

        assertEquals(bookingId, result.getId());
        assertEquals(BookingStatus.REJECTED, result.getStatus());

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void updateBookingShouldThrowWhenBookingNotFound() {
        Long bookingId = 1L;
        Long userId = 1L;

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> bookingService.updateBooking(bookingId, userId, true)
        );

        assertEquals("Бронь с id: " + bookingId + " не существует", exception.getMessage());
        verify(bookingRepository, never())
                .save(any(Booking.class));
    }

    @Test
    void updateBookingShouldThrowWhenUserNotFound() {
        Long bookingId = 1L;
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        Booking booking = new Booking();
        booking.setId(bookingId);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> bookingService.updateBooking(bookingId, userId, true)
        );

        assertEquals("User с id: " + userId + " не существует", exception.getMessage());
        verify(bookingRepository, never())
                .save(any(Booking.class));
    }

    @Test
    void updateBookingShouldThrowWhenUserIsNotOwner() {
        Long userId = 5L;
        User user = new User();
        user.setId(userId);

        Long bookerId = 4L;
        User booker = new User();
        booker.setId(bookerId);

        Long ownerId = 7L;
        Long itemId = 6L;
        Item item = new Item();
        item.setId(itemId);
        item.setOwner(ownerId);

        Long bookingId = 10L;
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        NotOwnerException exception = assertThrows(
                NotOwnerException.class,
                () -> bookingService.updateBooking(bookingId, userId, true)
        );

        assertEquals("Только владелец вещи может подтвердить или отклонить бронь", exception.getMessage());
        verify(bookingRepository, never())
                .save(any(Booking.class));
    }

    @Test
    void updateBookingShouldThrowWhenBookingAlreadyProcessed() {
        Long userId = 5L;
        User user = new User();
        user.setId(userId);

        Long bookerid = 4l;
        User booker = new User();
        booker.setId(bookerid);

        Long itemId = 6L;
        Item item = new Item();
        item.setId(itemId);
        item.setOwner(userId);

        Long bookingId = 10L;
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        ValidateException exception = assertThrows(
                ValidateException.class,
                () -> bookingService.updateBooking(bookingId, userId, true)
        );

        assertEquals("Бронирование уже было обработано", exception.getMessage());
        verify(bookingRepository, never())
                .save(any(Booking.class));
    }
}