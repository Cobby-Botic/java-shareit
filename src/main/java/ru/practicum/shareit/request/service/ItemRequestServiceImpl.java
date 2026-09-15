package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public ItemRequestDto createNewRequest(Long userId, NewItemRequestDto newItemRequestDto) {
        log.info("Добавление нового запроса");
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(
                "User с id: " + userId + " не существует"
        ));

        ItemRequest itemRequest = itemRequestRepository.save(ItemRequestMapper.toItemRequest(
                newItemRequestDto.getDescription(),
                user
        ));

        return ItemRequestMapper.itemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequestDto> getRequestByUser(Long userId) {
        return List.of();
    }

    @Override
    public List<ItemRequestDto> getAllRequests() {
        return List.of();
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId) {
        return null;
    }
}
