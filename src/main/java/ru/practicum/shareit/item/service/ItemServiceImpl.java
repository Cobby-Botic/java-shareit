package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.NotOwnerException;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        "Item с id: " + itemId + " не существует"
                ));

        ItemDto itemDto = ItemMapper.toItemDto(item);

        List<CommentDto> comments = commentRepository.findAllByItemId(itemId)
                .stream()
                .map(CommentMapper::toCommentDto)
                .toList();

        itemDto.setComments(comments);

        if (item.getOwner().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            List<Booking> bookings = bookingRepository.findAllByItemId(itemId);

            bookings.stream()
                    .filter(booking -> booking.getEnd().isBefore(now))
                    .max(Comparator.comparing(Booking::getEnd))
                    .ifPresent(booking ->
                            itemDto.setLastBooking(
                                    BookingMapper.toBookingShortDto(booking)
                            )
                    );

            bookings.stream()
                    .filter(booking -> booking.getStart().isAfter(now))
                    .min(Comparator.comparing(Booking::getStart))
                    .ifPresent(booking ->
                            itemDto.setNextBooking(
                                    BookingMapper.toBookingShortDto(booking)
                            )
                    );
        }

        return itemDto;
    }

    @Override
    public List<ItemDto> getAllItems(Long userId) {
        log.info("Поиск предметов пользователя: {}", userId);

        List<Item> items = itemRepository.findAllByOwner(userId);

        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        List<Booking> bookings = bookingRepository.findAllByItemIdIn(itemIds);

        Map<Long, List<Booking>> bookingsByItem = bookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getItem().getId()
                ));

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    ItemDto itemDto = ItemMapper.toItemDto(item);

                    List<Booking> itemBookings =
                            bookingsByItem.getOrDefault(item.getId(), List.of());

                    itemBookings.stream()
                            .filter(booking -> booking.getEnd().isBefore(now))
                            .max(Comparator.comparing(Booking::getEnd))
                            .ifPresent(booking ->
                                    itemDto.setLastBooking(
                                            BookingMapper.toBookingShortDto(booking)
                                    )
                            );

                    itemBookings.stream()
                            .filter(booking -> booking.getStart().isAfter(now))
                            .min(Comparator.comparing(Booking::getStart))
                            .ifPresent(booking ->
                                    itemDto.setNextBooking(
                                            BookingMapper.toBookingShortDto(booking)
                                    )
                            );

                    return itemDto;
                })
                .toList();
    }

    @Override
    @Transactional
    public ItemDto updateItem(ItemDto itemDto, Long userId, Long itemId) {

        Item currentItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        "Item с ID: " + itemId + " не найден"
                ));

        if (!currentItem.getOwner().equals(userId)) {
            throw new NotOwnerException(
                    "User: " + userId +
                            " не является владельцем вещи: " + itemId
            );
        }

        if (itemDto.getName() != null) {
            currentItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            currentItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            currentItem.setAvailable(itemDto.getAvailable());
        }

        Item savedItem = itemRepository.save(currentItem);

        log.info("Item после обновления: {}", savedItem);

        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto addItem(ItemDto itemDto, Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(
                "User с ID: " + userId + " не существует"
        ));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userId);
        item = itemRepository.save(item);
        log.info("Сохранен item с id {}", item.getId());

        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        List<Item> items = itemRepository.searchItemsByText(text);

        return items.stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemDto deleteItem(ItemDto itemDto, Long userId) {
        Item item = ItemMapper.toItem(itemDto);
        itemRepository.findById(item.getId())
                .orElseThrow(() -> new NotFoundException(
                "Item с ID " + item.getId() + " не существует"
                ));
        checkItemOwner(item, userId);
        itemRepository.delete(item);

        return ItemMapper.toItemDto(item);
    }

    @Transactional
    @Override
    public CommentDto createComment(Long userId, Long itemId, String text) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User с id: " + userId + " не существует"
                ));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        "Item с id: " + itemId + " не существует"
                ));

        boolean hasCompletedBooking = bookingRepository
                .existsByBookerIdAndItemIdAndEndBefore(
                        userId,
                        itemId,
                        LocalDateTime.now()
                );

        if (!hasCompletedBooking) {
            throw new ValidateException(
                    "Пользователь не может оставить отзыв на эту вещь"
            );
        }

        Comment savedComment = commentRepository.save(
                CommentMapper.toComment(item, user, text)
        );

        return CommentMapper.toCommentDto(savedComment);
    }

    private void checkItemOwner(Item item, Long userId) {
        if (!item.getOwner().equals(userId)) {
            throw new NotOwnerException("User : " + userId + " не является владельцем вещи с id: " + item.getId());
        }
    }
}