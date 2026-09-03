package ru.practicum.shareit.item.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto getItemById(Long id) {
        return itemRepository.getItemById(id);
    }

    @Override
    public Set<ItemDto> getAllItems(Long userId) {
        return itemRepository.getAllItems(userId);
    }

    @Override
    public ItemDto updateItem(ItemDto item, Long userId, Long itemId) {
        return itemRepository.updateItem(item, userId, itemId);
    }

    @Override
    public ItemDto addItem(ItemDto item, Long userId) {
        userRepository.getUserById(userId);
        return itemRepository.addItem(item, userId);
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.searchItem(text);
    }

    @Override
    public ItemDto deleteItem(ItemDto item, Long userId) {
        return itemRepository.deleteItem(item, userId);
    }
}