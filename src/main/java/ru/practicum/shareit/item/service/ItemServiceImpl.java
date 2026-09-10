package ru.practicum.shareit.item.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.NotOwnerException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto getItemById(Long id) {
        log.info("Поиск item c id: " + id);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Item с ID = " + id + " не существует"
                ));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllItems(Long userId) {
        log.info("Поиск предметов, пользователя: {}", userId);
        return itemRepository.findAll()
                .stream()
                .filter(item -> item.getOwner().equals(userId))
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public ItemDto updateItem(ItemDto itemDto, Long userId, Long itemId) {

        Item currentItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        "Item с ID: " + itemId + " не найден"
                ));

        if (!currentItem.getOwner().equals(userId)) {
            throw new NotOwnerException(
                    "User: " + userId +
                            " не является владельцем вещи: " + itemId
            );
        }

        if (itemDto.getName() != null) {
            currentItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            currentItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            currentItem.setAvailable(itemDto.getAvailable());
        }

        Item savedItem = itemRepository.save(currentItem);

        log.info("Item после обновления: {}", savedItem);

        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto addItem(ItemDto itemDto, Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(
                "User с ID: " + userId + " не существует"
        ));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userId);
        item = itemRepository.save(item);
        log.info("Сохранен item с id {}", item.getId());

        return ItemMapper.toItemDto(item);
    }

    @Override
    @Query("SELECT i FROM Item i " +
            "WHERE i.available = true " +
            "AND (LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')))")
    public List<ItemDto> searchItem(@Param("text") String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        List<Item> items = itemRepository.searchItemsByText(text);

        return items.stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemDto deleteItem(ItemDto itemDto, Long userId) {
        Item item = ItemMapper.toItem(itemDto);
        itemRepository.findById(item.getId())
                .orElseThrow(() -> new NotFoundException(
                "Item с ID " + item.getId() + " не существует"
                ));
        checkItemOwner(item, userId);
        itemRepository.delete(item);

        return ItemMapper.toItemDto(item);
    }

    private void checkItemOwner(Item item, Long userId) {
        if (!item.getOwner().equals(userId)) {
            throw new NotOwnerException("User : " + userId + " не является владельцем вещи с id: " + item.getId());
        }
    }
}