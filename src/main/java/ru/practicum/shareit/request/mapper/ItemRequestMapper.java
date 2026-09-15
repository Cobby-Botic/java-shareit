package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

public class ItemRequestMapper {

    public static ItemRequestDto itemRequestDto(ItemRequest itemRequest) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription()
        );
    }

    public static ItemRequest toItemRequest(String description, User user) {
        ItemRequest itemRequest = new ItemRequest();

        itemRequest.setDescription(description);
        itemRequest.setRequestorId(user);

        return itemRequest;
    }
}
