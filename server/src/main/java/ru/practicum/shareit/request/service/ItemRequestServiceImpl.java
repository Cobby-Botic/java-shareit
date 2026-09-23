package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public ItemRequestDto createNewRequest(
            Long userId,
            NewItemRequestDto newItemRequestDto
    ) {
        log.info("Добавление нового запроса");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User с id: " + userId + " не существует"
                ));

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(
                newItemRequestDto.getDescription(),
                user
        );

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);

        return ItemRequestMapper.toItemRequestDto(savedRequest, List.of());
    }

    @Override
    public List<ItemRequestDto> getRequestByUser(Long userId) {
        List<ItemRequest> requests =
                itemRequestRepository.findAllByRequestor_Id(userId);

        return toItemRequestDtos(requests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests() {
        List<ItemRequest> requests = itemRequestRepository.findAll();

        return toItemRequestDtos(requests);
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User с id: " + userId + " не найден"
                ));

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(
                        "Запрос с id: " + requestId + " не найден"
                ));

        List<ItemShortDto> items = itemRepository
                .findAllByRequest_Id(requestId)
                .stream()
                .map(ItemMapper::toItemShortDto)
                .toList();

        return ItemRequestMapper.toItemRequestDto(request, items);
    }

    private List<ItemRequestDto> toItemRequestDtos(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<ItemShortDto>> itemsByRequestId =
                itemRepository.findAllByRequest_IdIn(requestIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                item -> item.getRequest().getId(),
                                Collectors.mapping(
                                        ItemMapper::toItemShortDto,
                                        Collectors.toList()
                                )
                        ));

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), List.of())
                ))
                .toList();
    }
}
