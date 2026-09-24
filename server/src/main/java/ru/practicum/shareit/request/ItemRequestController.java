package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping()
    public ItemRequestDto createNewRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestBody NewItemRequestDto itemRequestDto) {
        log.info("Получен запрос на добавление нового запроса");
        return itemRequestService.createNewRequest(userId, itemRequestDto);
    }

    @GetMapping()
    public List<ItemRequestDto> getRequestsByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на поиск объявлений User ID: {}", userId);
        return itemRequestService.getRequestByUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests() {
        log.info("Получен запрос на поиск всех объявлений");
        return itemRequestService.getAllRequests();
    }

    @GetMapping("/{request_id}")
    public ItemRequestDto getRequestById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable("request_id") Long requestId) {

        log.info("Получен запрос на поиск объявления {} от User {}", requestId, userId);
        return itemRequestService.getRequestById(requestId, userId);
    }
}
