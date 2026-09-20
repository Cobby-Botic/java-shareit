package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                        item.getId(),
                        item.getName(),
                        item.getDescription(),
                item.getRequest() != null
                                ? item.getRequest().getId()
                                : null,
                item.getAvailable(),
                        null,
                        null,
                        List.of()
                );
    }

    public static Item toItem(
            ItemDto itemDto,
            Long ownerId,
            ItemRequest request
    ) {
        Item item = new Item();

        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(ownerId);
        item.setRequest(request);

        return item;
    }

    public static ItemShortDto toItemShortDto(Item item) {
        return new ItemShortDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null
        );
    }
}
