package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.ItemAlreadyExistsException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.NotOwnerException;
import ru.practicum.shareit.item.model.Item;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Repository
public class InMemoryItemRepository implements ItemRepository {

    private static Long itemId = 1L;
    private final Set<Item> items = new HashSet<>();

    @Override
    public ItemDto getItemById(Long id) {
        Item item = findItem(id);
        log.info("Поиск item: " + item);
        return createItemDto(item);
    }

    @Override
    public Set<ItemDto> getAllItems(Long userId) {
        Set<ItemDto> itemsDto = new HashSet<>();

        items.stream()
                .filter(item -> item.getOwner().equals(userId))
                .forEach(item -> itemsDto.add(createItemDto(item)));
        return itemsDto;
    }

    @Override
    public ItemDto addItem(Item item, Long userid) {
        checkOnExist(item);
        item.setId(itemId++);
        item.setOwner(userid);
        items.add(item);
        return createItemDto(item);
    }

    @Override
    public ItemDto updateItem(Item item, Long userId, Long itemId) {
        Item currentItem = findItem(itemId);
        checkItemOwner(currentItem, userId);

        if (item.getId() != null) {
            currentItem.setId(item.getId());
        }

        if (item.getOwner() != null) {
            currentItem.setOwner(item.getOwner());
        }

        if (item.getName() != null) {
            currentItem.setName(item.getName());
        }

        if (item.getDescription() != null) {
            currentItem.setDescription(item.getDescription());
        }

        if (item.getAvailable() != null) {
            currentItem.setAvailable(item.getAvailable());
        }
        log.info("Item : " + itemId + " updated");

        return createItemDto(currentItem);
    }

    @Override
    public Set<ItemDto> searchItem(String text) {
        if (text.isBlank()) {
            return new HashSet<>();
        }

        Set<ItemDto> itemsDto = new HashSet<>();

        items.stream()
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase())
                        && item.getAvailable().equals(true))
                .forEach(item -> itemsDto.add(createItemDto(item)));

        return itemsDto;
    }

    @Override
    public ItemDto deleteItem(Item item, Long userId) {
        findItem(item.getId());
        checkItemOwner(item, userId);
        items.remove(item);
        log.info("Item : " + item.getId() + " удален");

        return createItemDto(item);
    }

    private ItemDto createItemDto(Item item) {
        return new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable(), item.getOwner());
    }

    private Item findItem(Long id) {
        return items.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ItemNotFoundException("Item: " + id + " не найден"));
    }

    private void checkOnExist(Item item) {
        if (items.stream()
                .anyMatch(currentItem -> currentItem.getId().equals(item.getId()))) {
            throw new ItemAlreadyExistsException("Item : " + item.getId() + " уже существует");
        }
    }

    private void checkItemOwner(Item item, Long userId) {
        if (!item.getOwner().equals(userId)) {
            throw new NotOwnerException("User : " + userId + " не является владельцем вещи с id: " + item.getId());
        }
    }
}
