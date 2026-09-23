package ru.practicum.shareit.requests.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void createNewRequestShouldCreateRequest() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setName("Daniel");
        user.setEmail("daniel@test.ru");

        NewItemRequestDto newRequest = new NewItemRequestDto();
        newRequest.setDescription("Нужна дрель");

        ItemRequest savedRequest = new ItemRequest();
        savedRequest.setId(10L);
        savedRequest.setDescription("Нужна дрель");
        savedRequest.setRequestor(user);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(itemRequestRepository.save(any(ItemRequest.class)))
                .thenReturn(savedRequest);

        ItemRequestDto result =
                itemRequestService.createNewRequest(userId, newRequest);

        assertEquals(10L, result.getId());
        assertEquals("Нужна дрель", result.getDescription());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void getAllRequestsShouldReturnRequests() {
        ItemRequest firstRequest = new ItemRequest();
        firstRequest.setId(10L);
        firstRequest.setDescription("Нужна дрель");

        ItemRequest secondRequest = new ItemRequest();
        secondRequest.setId(11L);
        secondRequest.setDescription("Нужен молоток");

        when(itemRequestRepository.findAll())
                .thenReturn(List.of(firstRequest, secondRequest));

        when(itemRepository.findAllByRequest_IdIn(List.of(10L, 11L)))
                .thenReturn(List.of());

        List<ItemRequestDto> result =
                itemRequestService.getAllRequests();

        assertEquals(2, result.size());

        assertEquals(10L, result.get(0).getId());
        assertEquals("Нужна дрель", result.get(0).getDescription());

        assertEquals(11L, result.get(1).getId());
        assertEquals("Нужен молоток", result.get(1).getDescription());
    }

    @Test
    void getAllRequestsShouldReturnEmptyListWhenNoRequests() {
        when(itemRequestRepository.findAll())
                .thenReturn(List.of());

        List<ItemRequestDto> result =
                itemRequestService.getAllRequests();

        assertTrue(result.isEmpty());

        verify(itemRepository, never())
                .findAllByRequest_IdIn(anyList());
    }

    @Test
    void getRequestByIdShouldReturnRequest() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setName("Daniel");
        user.setEmail("daniel@test.ru");

        Long itemRequestId = 10L;

        ItemRequest savedRequest = new ItemRequest();
        savedRequest.setId(itemRequestId);
        savedRequest.setDescription("Нужна дрель");
        savedRequest.setRequestor(user);

        Item item = new Item();
        item.setId(20L);
        item.setName("Дрель");
        item.setDescription("Обычная дрель");
        item.setAvailable(true);
        item.setOwner(2L);
        item.setRequest(savedRequest);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(itemRequestRepository.findById(itemRequestId))
                .thenReturn(Optional.of(savedRequest));

        when(itemRepository.findAllByRequest_Id(itemRequestId))
                .thenReturn(List.of(item));

        ItemRequestDto result =
                itemRequestService.getRequestById(itemRequestId, userId);

        assertEquals(10L, result.getId());
        assertEquals("Нужна дрель", result.getDescription());

        assertEquals(1, result.getItems().size());
        assertEquals(20L, result.getItems().get(0).getId());
        assertEquals("Дрель", result.getItems().get(0).getName());
        assertEquals("Обычная дрель", result.getItems().get(0).getDescription());
        assertTrue(result.getItems().get(0).getAvailable());
    }

    @Test
    void createNewRequestShouldThrowWhenUserNotFound() {
        Long userId = 1L;

        NewItemRequestDto newRequest = new NewItemRequestDto();
        newRequest.setDescription("Нужна дрель");

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.createNewRequest(userId, newRequest)
        );

        assertEquals(
                "User с id: " + userId + " не существует",
                exception.getMessage()
        );

        verify(itemRequestRepository, never())
                .save(any(ItemRequest.class));
    }

    @Test
    void getRequestShouldThrowWhenRequestNotFound() {
        Long userId = 1L;
        Long requestId = 10L;

        User user = new User();
        user.setId(userId);
        user.setName("Daniel");
        user.setEmail("daniel@test.ru");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getRequestById(requestId, userId)
        );

        assertEquals(
                "Запрос с id: " + requestId + " не найден",
                exception.getMessage()
        );

        verify(itemRepository, never())
                .findAllByRequest_Id(anyLong());
    }

    @Test
    void getRequestShouldThrowWhenUserNotFound() {
        Long userId = 1L;
        Long requestId = 10L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getRequestById(requestId, userId)
                );

        assertEquals(
                "User с id: " + userId + " не найден",
                exception.getMessage()
        );

        verify(itemRequestRepository, never())
                .findById(anyLong());
    }

    @Test
    void getRequestByUserShouldReturnRequests() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setName("Daniel");
        user.setEmail("daniel@test.ru");

        Long itemRequestId = 10L;

        ItemRequest savedRequest = new ItemRequest();
        savedRequest.setId(itemRequestId);
        savedRequest.setDescription("Нужна дрель");
        savedRequest.setRequestor(user);

        Item item = new Item();
        item.setId(20L);
        item.setName("Дрель");
        item.setDescription("Обычная дрель");
        item.setAvailable(true);
        item.setOwner(2L);
        item.setRequest(savedRequest);

        when(itemRequestRepository.findAllByRequestor_Id(userId))
                .thenReturn(List.of(savedRequest));

        when(itemRepository.findAllByRequest_IdIn(List.of(itemRequestId)))
                .thenReturn(List.of(item));

        List<ItemRequestDto> result =
                itemRequestService.getRequestByUser(userId);

        assertEquals(1, result.size());

        ItemRequestDto requestDto = result.get(0);

        assertEquals(itemRequestId, requestDto.getId());
        assertEquals("Нужна дрель", requestDto.getDescription());

        assertEquals(1, requestDto.getItems().size());

        ItemShortDto itemDto = requestDto.getItems().get(0);

        assertEquals(20L, itemDto.getId());
        assertEquals("Дрель", itemDto.getName());
        assertEquals("Обычная дрель", itemDto.getDescription());
        assertTrue(itemDto.getAvailable());
    }

    @Test
    void getRequestByUserShouldReturnRequests2() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setName("Daniel");
        user.setEmail("daniel@test.ru");

        Long itemRequestId = 10L;

        ItemRequest savedRequest = new ItemRequest();
        savedRequest.setId(itemRequestId);
        savedRequest.setDescription("Нужна дрель");
        savedRequest.setRequestor(user);

        Long itemRequestId2 = 11L;

        ItemRequest savedRequest2 = new ItemRequest();
        savedRequest2.setId(itemRequestId2);
        savedRequest2.setDescription("Нужен молоток");
        savedRequest2.setRequestor(user);

        Item item = new Item();
        item.setId(20L);
        item.setName("Дрель");
        item.setDescription("Обычная дрель");
        item.setAvailable(true);
        item.setOwner(2L);
        item.setRequest(savedRequest);

        when(itemRequestRepository.findAllByRequestor_Id(userId))
                .thenReturn(List.of(savedRequest, savedRequest2));

        when(itemRepository.findAllByRequest_IdIn(List.of(itemRequestId, itemRequestId2)))
                .thenReturn(List.of(item));

        List<ItemRequestDto> result =
                itemRequestService.getRequestByUser(userId);

        assertEquals(2, result.size());


        ItemRequestDto firstRequest = result.get(0);

        assertEquals(itemRequestId, firstRequest.getId());
        assertEquals("Нужна дрель", firstRequest.getDescription());

        assertEquals(1, firstRequest.getItems().size());

        ItemShortDto itemDto = firstRequest.getItems().get(0);

        assertEquals(20L, itemDto.getId());
        assertEquals("Дрель", itemDto.getName());
        assertEquals("Обычная дрель", itemDto.getDescription());
        assertTrue(itemDto.getAvailable());

        ItemRequestDto secondRequest = result.get(1);

        assertEquals(itemRequestId2, secondRequest.getId());
        assertEquals("Нужен молоток", secondRequest.getDescription());
        assertTrue(secondRequest.getItems().isEmpty());
    }
}