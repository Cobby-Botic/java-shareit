package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.Set;

public interface ItemRepository {

    ItemDto getItemById(Long id);

    Set<ItemDto> getAllItems(Long userId);

    ItemDto addItem(ItemDto itemDto, Long userid);

    ItemDto updateItem(ItemDto itemDto, Long userId, Long itemId);

    List<ItemDto> searchItem(String text);

    ItemDto deleteItem(ItemDto itemDto, Long userId);
}
