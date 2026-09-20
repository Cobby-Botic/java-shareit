package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestClient requestClient;

    @PostMapping()
    public ResponseEntity<Object> createNewRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestBody @Valid ItemRequestDto itemRequestDto) {
        log.info("Получен запрос на добавление нового запроса");
        return requestClient.createNewRequest(userId, itemRequestDto);
    }

    @GetMapping()
    public ResponseEntity<Object> getRequestsByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на поиск объявлений User ID: {}", userId);
        return requestClient.getRequestByUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests() {
        log.info("Получен запрос на поиск всех объявлений");
        return requestClient.getAllRequests();
    }

    @GetMapping("/{request_id}")
    public ResponseEntity<Object> getRequestById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable("request_id") Long requestId) {

        log.info("Получен запрос на поиск объявления {} от User {}", requestId, userId);
        return requestClient.getRequestById(requestId, userId);
    }
}
