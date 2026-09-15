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
    public List<ItemRequestDto> getRequestsByUser() {
        return null;
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests() {
        return null;
    }

    @GetMapping("{request_id}")
    public ItemRequestDto getRequestById(@RequestParam("request_id") Long requestId) {
        return null;
    }

}
