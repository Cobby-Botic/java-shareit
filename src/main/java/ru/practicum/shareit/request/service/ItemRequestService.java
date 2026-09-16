package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    public ItemRequestDto createNewRequest(Long userId, NewItemRequestDto itemRequestDto);

    public List<ItemRequestDto> getRequestByUser(Long userId);

    public List<ItemRequestDto> getAllRequests();

    public ItemRequestDto getRequestById(Long requestId, Long userId);
}
