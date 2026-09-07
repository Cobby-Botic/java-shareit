package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.Set;

public interface ItemService {

    ItemDto getItemById(Long id);

    Set<ItemDto> getAllItems(Long userId);

    ItemDto updateItem(ItemDto item, Long userId, Long itemId);

    ItemDto addItem(ItemDto item, Long userId);

    List<ItemDto> searchItem(String text);

    ItemDto deleteItem(ItemDto item, Long userId);
}
