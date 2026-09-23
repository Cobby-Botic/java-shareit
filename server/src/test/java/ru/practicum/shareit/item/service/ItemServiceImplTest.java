package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.NotOwnerException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void searchItemShouldReturnItems() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Хорошая дрель");
        item.setAvailable(true);

        when(itemRepository.searchItemsByText("дрель"))
                .thenReturn(List.of(item));

        List<ItemDto> result = itemService.searchItem("дрель");

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void searchItemShouldReturnEmptyListWhenTextIsBlank() {
        List<ItemDto> result = itemService.searchItem("");

        assertTrue(result.isEmpty());

        verify(itemRepository, never())
                .searchItemsByText(anyString());
    }

    @Test
    void deleteItemShouldDeleteItem() {
        Long userId = 1L;
        Long itemId = 10L;

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(userId);
        item.setName("Дрель");
        item.setDescription("Описание");
        item.setAvailable(true);

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        ItemDto result = itemService.deleteItem(itemId, userId);

        assertEquals(itemId, result.getId());

        verify(itemRepository).delete(item);
    }

    @Test
    void deleteItemShouldThrowWhenItemNotFound() {
        Long userId = 1L;
        Long itemId = 10L;

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.deleteItem(itemId, userId)
        );

        assertEquals(
                "Item с ID " + itemId + " не существует",
                exception.getMessage()
        );

        verify(itemRepository, never()).delete(any());
    }

    @Test
    void deleteItemShouldThrowWhenUserIsNotOwner() {
        Long userId = 1L;
        Long itemId = 10L;

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(2L);

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        NotOwnerException exception = assertThrows(
                NotOwnerException.class,
                () -> itemService.deleteItem(itemId, userId)
        );

        assertEquals(
                "User : " + userId +
                        " не является владельцем вещи с id: " + itemId,
                exception.getMessage()
        );

        verify(itemRepository, never()).delete(any());
    }

    @Test
    void updateItemShouldUpdateItem() {
        Long userId = 1L;
        Long itemId = 10L;

        Item currentItem = new Item();
        currentItem.setId(itemId);
        currentItem.setOwner(userId);
        currentItem.setName("Old name");
        currentItem.setDescription("Old description");
        currentItem.setAvailable(true);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("New name");
        updateDto.setDescription("New description");
        updateDto.setAvailable(false);

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(currentItem));

        when(itemRepository.save(any(Item.class)))
                .thenReturn(currentItem);

        ItemDto result = itemService.updateItem(
                updateDto,
                userId,
                itemId
        );

        assertEquals(itemId, result.getId());
        assertEquals("New name", result.getName());
        assertEquals("New description", result.getDescription());
        assertFalse(result.getAvailable());

        verify(itemRepository).save(currentItem);
    }

    @Test
    void updateItemShouldThrowWhenItemNotFound() {
        Long userId = 1L;
        Long itemId = 10L;

        ItemDto updateDto = new ItemDto();
        updateDto.setName("New name");

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.updateItem(
                        updateDto,
                        userId,
                        itemId
                )
        );

        assertEquals(
                "Item с ID: " + itemId + " не найден",
                exception.getMessage()
        );

        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItemShouldThrowWhenUserIsNotOwner() {
        Long userId = 1L;
        Long itemId = 10L;

        Item currentItem = new Item();
        currentItem.setId(itemId);
        currentItem.setOwner(2L);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("New name");

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(currentItem));

        NotOwnerException exception = assertThrows(
                NotOwnerException.class,
                () -> itemService.updateItem(
                        updateDto,
                        userId,
                        itemId
                )
        );

        assertEquals(
                "User: " + userId +
                        " не является владельцем вещи: " + itemId,
                exception.getMessage()
        );

        verify(itemRepository, never()).save(any());
    }

    @Test
    void addItemShouldSaveNewItemWithoutRequest() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Хорошая дрель");
        itemDto.setAvailable(true);

        Item savedItem = new Item();
        savedItem.setId(10L);
        savedItem.setName("Дрель");
        savedItem.setDescription("Хорошая дрель");
        savedItem.setAvailable(true);
        savedItem.setOwner(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(itemRepository.save(any(Item.class)))
                .thenReturn(savedItem);

        ItemDto result = itemService.addItem(itemDto, userId);

        assertEquals(10L, result.getId());
        assertEquals("Дрель", result.getName());
        assertEquals("Хорошая дрель", result.getDescription());
        assertTrue(result.getAvailable());

        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void addItemShouldSaveNewItemWithRequest() {
        Long userId = 1L;
        Long requestId = 5L;

        User user = new User();
        user.setId(userId);

        ItemRequest request = new ItemRequest();
        request.setId(requestId);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Хорошая дрель");
        itemDto.setAvailable(true);
        itemDto.setRequestId(requestId);

        Item savedItem = new Item();
        savedItem.setId(10L);
        savedItem.setName("Дрель");
        savedItem.setDescription("Хорошая дрель");
        savedItem.setAvailable(true);
        savedItem.setOwner(userId);
        savedItem.setRequest(request);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.of(request));

        when(itemRepository.save(any(Item.class)))
                .thenReturn(savedItem);

        ItemDto result = itemService.addItem(itemDto, userId);

        assertEquals(10L, result.getId());
        assertEquals("Дрель", result.getName());
        assertEquals(requestId, result.getRequestId());

        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void addItemShouldThrowWhenUserNotFound() {
        Long userId = 1L;

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.addItem(itemDto, userId)
        );

        assertEquals(
                "User с id: " + userId + " не существует",
                exception.getMessage()
        );

        verify(itemRepository, never()).save(any());
    }

    @Test
    void addItemShouldThrowWhenRequestNotFound() {
        Long userId = 1L;
        Long requestId = 5L;

        User user = new User();
        user.setId(userId);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Описание");
        itemDto.setAvailable(true);
        itemDto.setRequestId(requestId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.addItem(itemDto, userId)
        );

        assertEquals(
                "Запрос с id: " + requestId + " не существует",
                exception.getMessage()
        );

        verify(itemRepository, never()).save(any());
    }

    @Test
    void getItemByIdShouldReturnItem() {
        Long itemId = 10L;
        Long userId = 1L;

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(2L);
        item.setName("Дрель");
        item.setDescription("Хорошая дрель");
        item.setAvailable(true);

        Comment comment = new Comment();
        comment.setId(20L);
        comment.setText("Хорошая вещь");
        comment.setItem(item);

        User author = new User();
        author.setId(3L);
        author.setName("Alex");
        comment.setAuthor(author);

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        when(commentRepository.findAllByItemId(itemId))
                .thenReturn(List.of(comment));

        ItemDto result = itemService.getItemById(itemId, userId);

        assertEquals(itemId, result.getId());
        assertEquals("Дрель", result.getName());
        assertEquals(1, result.getComments().size());

        verify(bookingRepository, never())
                .findAllByItemId(anyLong());
    }

    @Test
    void getItemByIdShouldReturnLastAndNextBookingForOwner() {
        Long ownerId = 1L;
        Long itemId = 10L;

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(ownerId);
        item.setName("Дрель");
        item.setDescription("Описание");
        item.setAvailable(true);

        User booker = new User();
        booker.setId(2L);

        Booking lastBooking = new Booking();
        lastBooking.setId(20L);
        lastBooking.setItem(item);
        lastBooking.setBooker(booker);
        lastBooking.setStart(LocalDateTime.now().minusDays(3));
        lastBooking.setEnd(LocalDateTime.now().minusDays(2));

        Booking nextBooking = new Booking();
        nextBooking.setId(30L);
        nextBooking.setItem(item);
        nextBooking.setBooker(booker);
        nextBooking.setStart(LocalDateTime.now().plusDays(2));
        nextBooking.setEnd(LocalDateTime.now().plusDays(3));

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        when(commentRepository.findAllByItemId(itemId))
                .thenReturn(List.of());

        when(bookingRepository.findAllByItemId(itemId))
                .thenReturn(List.of(lastBooking, nextBooking));

        ItemDto result = itemService.getItemById(itemId, ownerId);

        assertEquals(itemId, result.getId());

        assertNotNull(result.getLastBooking());
        assertEquals(lastBooking.getId(), result.getLastBooking().getId());

        assertNotNull(result.getNextBooking());
        assertEquals(nextBooking.getId(), result.getNextBooking().getId());
    }

    @Test
    void getItemByIdShouldThrowWhenItemNotFound() {
        Long itemId = 10L;
        Long userId = 1L;

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.getItemById(itemId, userId)
        );

        assertEquals(
                "Item с id: " + itemId + " не существует",
                exception.getMessage()
        );

        verify(commentRepository, never())
                .findAllByItemId(anyLong());
    }

    @Test
    void getAllItemsShouldReturnItemsWithBookings() {
        Long ownerId = 1L;

        Item item = new Item();
        item.setId(10L);
        item.setOwner(ownerId);
        item.setName("Дрель");
        item.setDescription("Описание");
        item.setAvailable(true);

        User booker = new User();
        booker.setId(2L);

        Booking lastBooking = new Booking();
        lastBooking.setId(20L);
        lastBooking.setItem(item);
        lastBooking.setBooker(booker);
        lastBooking.setStart(LocalDateTime.now().minusDays(3));
        lastBooking.setEnd(LocalDateTime.now().minusDays(2));

        Booking nextBooking = new Booking();
        nextBooking.setId(30L);
        nextBooking.setItem(item);
        nextBooking.setBooker(booker);
        nextBooking.setStart(LocalDateTime.now().plusDays(2));
        nextBooking.setEnd(LocalDateTime.now().plusDays(3));

        when(itemRepository.findAllByOwner(ownerId))
                .thenReturn(List.of(item));

        when(bookingRepository.findAllByItemIdIn(List.of(10L)))
                .thenReturn(List.of(lastBooking, nextBooking));

        List<ItemDto> result = itemService.getAllItems(ownerId);

        assertEquals(1, result.size());

        ItemDto resultItem = result.get(0);

        assertEquals(10L, resultItem.getId());

        assertNotNull(resultItem.getLastBooking());
        assertEquals(
                lastBooking.getId(),
                resultItem.getLastBooking().getId()
        );

        assertNotNull(resultItem.getNextBooking());
        assertEquals(
                nextBooking.getId(),
                resultItem.getNextBooking().getId()
        );
    }

    @Test
    void getAllItemsShouldReturnEmptyList() {
        Long userId = 1L;

        when(itemRepository.findAllByOwner(userId))
                .thenReturn(List.of());

        List<ItemDto> result = itemService.getAllItems(userId);

        assertTrue(result.isEmpty());

        verify(bookingRepository, never())
                .findAllByItemIdIn(anyList());
    }

    @Test
    void createCommentShouldCreateComment() {
        Long userId = 1L;
        Long itemId = 10L;

        User user = new User();
        user.setId(userId);
        user.setName("Daniel");

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(2L);

        String text = "Отличная вещь";

        Comment savedComment = new Comment();
        savedComment.setId(20L);
        savedComment.setText(text);
        savedComment.setAuthor(user);
        savedComment.setItem(item);
        savedComment.setCreated(LocalDateTime.now());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(
                eq(userId),
                eq(itemId),
                any(LocalDateTime.class)
        )).thenReturn(true);

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(savedComment);

        CommentDto result =
                itemService.createComment(userId, itemId, text);

        assertEquals(20L, result.getId());
        assertEquals(text, result.getText());

        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createCommentShouldThrowWhenUserNotFound() {
        Long userId = 1L;
        Long itemId = 10L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.createComment(
                        userId,
                        itemId,
                        "Комментарий"
                )
        );

        assertEquals(
                "User с id: " + userId + " не существует",
                exception.getMessage()
        );

        verify(commentRepository, never()).save(any());
    }

    @Test
    void createCommentShouldThrowWhenItemNotFound() {
        Long userId = 1L;
        Long itemId = 10L;

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.createComment(
                        userId,
                        itemId,
                        "Комментарий"
                )
        );

        assertEquals(
                "Item с id: " + itemId + " не существует",
                exception.getMessage()
        );

        verify(commentRepository, never()).save(any());
    }

    @Test
    void createCommentShouldThrowWhenUserHasNoCompletedBooking() {
        Long userId = 1L;
        Long itemId = 10L;

        User user = new User();
        user.setId(userId);

        Item item = new Item();
        item.setId(itemId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(
                eq(userId),
                eq(itemId),
                any(LocalDateTime.class)
        )).thenReturn(false);

        ValidateException exception = assertThrows(
                ValidateException.class,
                () -> itemService.createComment(
                        userId,
                        itemId,
                        "Комментарий"
                )
        );

        assertEquals(
                "Пользователь не может оставить отзыв на эту вещь",
                exception.getMessage()
        );

        verify(commentRepository, never()).save(any());
    }
}
