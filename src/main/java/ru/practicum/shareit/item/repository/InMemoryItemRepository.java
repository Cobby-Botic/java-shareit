package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.NotOwnerException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Slf4j
@Repository
public class InMemoryItemRepository implements ItemRepository {

    private static Long itemId = 1L;
    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public ItemDto getItemById(Long id) {
        Item item = findItem(id);
        log.info("Поиск item: " + item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public Set<ItemDto> getAllItems(Long userId) {
        Set<ItemDto> itemsDto = new HashSet<>();

        items.values().stream()
                        .filter(item -> item.getOwner().equals(userId))
                                .forEach(item -> itemsDto.add(ItemMapper.toItemDto(item)));
        return itemsDto;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Long userid) {
        Item item = ItemMapper.toItem(itemDto);
        checkOnExist(item);
        item.setId(itemId++);
        item.setOwner(userid);
        items.put(item.getId(), item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long userId, Long itemId) {
        Item item = ItemMapper.toItem(itemDto);
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

        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> searchItem(String text) {

        List<ItemDto> itemsDto = new ArrayList<>();

        items.values().stream()
                        .filter(item ->
                                (item.getName().toLowerCase().contains(text.toLowerCase())
                                || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                                        && Boolean.TRUE.equals(item.getAvailable()))
                                .forEach(item -> itemsDto.add(ItemMapper.toItemDto(item)));

        return itemsDto;
    }

    @Override
    public ItemDto deleteItem(ItemDto itemDto, Long userId) {
        Item item = ItemMapper.toItem(itemDto);
        findItem(item.getId());
        checkItemOwner(item, userId);
        items.remove(item.getId());
        log.info("Item : " + item.getId() + " удален");

        return ItemMapper.toItemDto(item);
    }

    private Item findItem(Long id) {
        if (!items.containsKey(id)) {
            throw new NotFoundException("Item: " + id + " не найден");
        }

        return items.get(id);
    }

    private void checkOnExist(Item item) {
        if (items.containsKey(item.getId())) {
            throw new AlreadyExistsException("Item : " + item.getId() + " уже существует");
        }
    }

    private void checkItemOwner(Item item, Long userId) {
        if (!item.getOwner().equals(userId)) {
            throw new NotOwnerException("User : " + userId + " не является владельцем вещи с id: " + item.getId());
        }
    }
}
