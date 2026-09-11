package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public BookingDto addBooking(NewBookingDto bookingDto, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User с ID: " + userId + " не существует"
                ));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException(
                        "Item с ID: " + bookingDto.getItemId() + " не существует"
                ));

        if (item.getOwner().equals(userId)) {
            throw new ValidateException(
                    "Владелец вещи не может бронировать собственную вещь"
            );
        }

        if (!item.getAvailable()) {
            throw new ValidateException(
                    "Нельзя забронировать недоступную вещь"
            );
        }

        if (!bookingDto.getStart().isBefore(bookingDto.getEnd())) {
            throw new ValidateException(
                    "Дата начала должна быть раньше даты окончания"
            );
        }

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);

        return BookingMapper.toBookingDto(savedBooking);
    }

    @Override
    public BookingDto getBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(
                        "Бронь с id: " + bookingId + " не существует"
                ));

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User с id: " + userId + " не существует"
                ));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotOwnerException(
                    "Пользователь не имеет доступа к этому бронированию"
            );
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User с id: " + userId + " не существует"
                ));

        return bookingRepository.findAllByBookerId(userId)
                .stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public List<BookingDto> getBookingsByOwner(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User с id: " + userId + " не существует"
                ));

        return bookingRepository.findAllByItemOwner(userId)
                .stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Transactional
    @Override
    public BookingDto updateBooking(Long bookingId, Long userId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ForbiddenException(
                        "Бронь с id: " + bookingId + " не существует"
                ));

        userRepository.findById(userId)
                .orElseThrow(() -> new ForbiddenException(
                        "User с id: " + userId + " не существует"
                ));

        Item item = booking.getItem();

        if (!item.getOwner().equals(userId)) {
            throw new NotOwnerException(
                    "Только владелец вещи может подтвердить или отклонить бронь"
            );
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        Booking savedBooking = bookingRepository.save(booking);

        return BookingMapper.toBookingDto(savedBooking);
    }
}
