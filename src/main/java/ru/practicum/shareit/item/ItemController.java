package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable("itemId") Long itemId,
                               @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getAllItems(userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable("itemId") Long itemId,
                              @RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("PATCH itemDto: {}", itemDto);
        return itemService.updateItem(itemDto, userId, itemId);
    }

    @PostMapping
    public ItemDto addItem(@Valid @RequestBody ItemDto item,
                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Добавление item: {}, owner {}", item, userId);
        return itemService.addItem(item, userId);
    }

    @DeleteMapping
    public ItemDto deleteItem(@RequestBody ItemDto item,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.deleteItem(item, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam("text") String text,
                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.searchItem(text);
    }
}
