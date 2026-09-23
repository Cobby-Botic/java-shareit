package ru.practicum.shareit.integration.Item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.service.item.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User owner;
    private User booker;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.ru");
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@test.ru");
        booker = userRepository.save(booker);
    }

    @Test
    void addItemShouldSaveItemToDatabase() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Обычная дрель");
        itemDto.setAvailable(true);

        ItemDto result = itemService.addItem(itemDto, owner.getId());

        assertNotNull(result.getId());
        assertEquals("Дрель", result.getName());
        assertEquals("Обычная дрель", result.getDescription());
        assertTrue(result.getAvailable());

        Item savedItem = itemRepository.findById(result.getId()).orElseThrow();

        assertEquals("Дрель", savedItem.getName());
        assertEquals(owner.getId(), savedItem.getOwner());
        assertTrue(savedItem.getAvailable());
    }

    @Test
    void getItemByIdShouldReturnItemWithBookingsAndComments() {
        Item item = createItem(
                "Дрель",
                "Обычная дрель",
                owner.getId()
        );

        Booking lastBooking = new Booking();
        lastBooking.setItem(item);
        lastBooking.setBooker(booker);
        lastBooking.setStart(LocalDateTime.now().minusDays(3));
        lastBooking.setEnd(LocalDateTime.now().minusDays(2));
        lastBooking.setStatus(BookingStatus.APPROVED);
        lastBooking = bookingRepository.save(lastBooking);

        Booking nextBooking = new Booking();
        nextBooking.setItem(item);
        nextBooking.setBooker(booker);
        nextBooking.setStart(LocalDateTime.now().plusDays(2));
        nextBooking.setEnd(LocalDateTime.now().plusDays(3));
        nextBooking.setStatus(BookingStatus.APPROVED);
        nextBooking = bookingRepository.save(nextBooking);

        ru.practicum.shareit.item.model.Comment comment =
                new ru.practicum.shareit.item.model.Comment();

        comment.setItem(item);
        comment.setAuthor(booker);
        comment.setText("Отличная дрель");
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);

        ItemDto result = itemService.getItemById(
                item.getId(),
                owner.getId()
        );

        assertEquals(item.getId(), result.getId());
        assertEquals("Дрель", result.getName());

        assertNotNull(result.getLastBooking());
        assertEquals(lastBooking.getId(), result.getLastBooking().getId());

        assertNotNull(result.getNextBooking());
        assertEquals(nextBooking.getId(), result.getNextBooking().getId());

        assertNotNull(result.getComments());
        assertEquals(1, result.getComments().size());
        assertEquals(
                "Отличная дрель",
                result.getComments().get(0).getText()
        );
    }

    @Test
    void getAllItemsShouldReturnOwnerItems() {
        createItem(
                "Дрель",
                "Обычная дрель",
                owner.getId()
        );

        createItem(
                "Молоток",
                "Обычный молоток",
                owner.getId()
        );

        List<ItemDto> result = itemService.getAllItems(owner.getId());

        assertEquals(2, result.size());

        assertTrue(result.stream()
                .anyMatch(item -> item.getName().equals("Дрель")));

        assertTrue(result.stream()
                .anyMatch(item -> item.getName().equals("Молоток")));
    }

    @Test
    void updateItemShouldUpdateItemInDatabase() {
        Item item = createItem(
                "Старая дрель",
                "Старое описание",
                owner.getId()
        );

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Новая дрель");
        updateDto.setDescription("Новое описание");
        updateDto.setAvailable(false);

        ItemDto result = itemService.updateItem(
                updateDto,
                owner.getId(),
                item.getId()
        );

        assertEquals("Новая дрель", result.getName());
        assertEquals("Новое описание", result.getDescription());
        assertFalse(result.getAvailable());

        Item updatedItem = itemRepository
                .findById(item.getId())
                .orElseThrow();

        assertEquals("Новая дрель", updatedItem.getName());
        assertEquals("Новое описание", updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());
    }

    @Test
    void searchItemShouldReturnMatchingAvailableItem() {
        createItem(
                "Перфоратор",
                "Мощный инструмент для ремонта",
                owner.getId()
        );

        createItem(
                "Молоток",
                "Обычный инструмент",
                owner.getId()
        );

        List<ItemDto> result = itemService.searchItem("перфоратор");

        assertEquals(1, result.size());
        assertEquals("Перфоратор", result.get(0).getName());
    }

    @Test
    void deleteItemShouldDeleteItemFromDatabase() {
        Item item = createItem(
                "Дрель",
                "Обычная дрель",
                owner.getId()
        );

        ItemDto result = itemService.deleteItem(
                item.getId(),
                owner.getId()
        );

        assertEquals(item.getId(), result.getId());
        assertFalse(itemRepository.existsById(item.getId()));
    }

    @Test
    void createCommentShouldSaveCommentToDatabase() {
        Item item = createItem(
                "Дрель",
                "Обычная дрель",
                owner.getId()
        );

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        CommentDto result = itemService.createComment(
                booker.getId(),
                item.getId(),
                "Отличная дрель"
        );

        assertNotNull(result.getId());
        assertEquals("Отличная дрель", result.getText());

        List<ru.practicum.shareit.item.model.Comment> comments =
                commentRepository.findAllByItemId(item.getId());

        assertEquals(1, comments.size());
        assertEquals("Отличная дрель", comments.get(0).getText());
        assertEquals(booker.getId(), comments.get(0).getAuthor().getId());
    }

    private Item createItem(
            String name,
            String description,
            Long ownerId
    ) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setOwner(ownerId);
        item.setAvailable(true);

        return itemRepository.save(item);
    }
}
