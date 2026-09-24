package ru.practicum.shareit.integration.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User requestor;
    private User owner;

    @BeforeEach
    void setUp() {
        requestor = new User();
        requestor.setName("Requestor");
        requestor.setEmail("requestor@test.ru");
        requestor = userRepository.save(requestor);

        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.ru");
        owner = userRepository.save(owner);
    }

    @Test
    void createNewRequestShouldSaveRequestToDatabase() {
        NewItemRequestDto newRequestDto = new NewItemRequestDto();
        newRequestDto.setDescription("Нужна дрель");

        ItemRequestDto result = itemRequestService.createNewRequest(
                requestor.getId(),
                newRequestDto
        );

        assertNotNull(result.getId());
        assertEquals("Нужна дрель", result.getDescription());
        assertNotNull(result.getCreated());
        assertNotNull(result.getItems());
        assertTrue(result.getItems().isEmpty());

        ItemRequest savedRequest = itemRequestRepository
                .findById(result.getId())
                .orElseThrow();

        assertEquals("Нужна дрель", savedRequest.getDescription());
        assertEquals(
                requestor.getId(),
                savedRequest.getRequestor().getId()
        );
        assertNotNull(savedRequest.getCreated());
    }

    @Test
    void getRequestByUserShouldReturnUserRequests() {
        ItemRequest firstRequest = createRequest(
                "Нужна дрель",
                requestor
        );

        ItemRequest secondRequest = createRequest(
                "Нужен молоток",
                requestor
        );

        List<ItemRequestDto> result =
                itemRequestService.getRequestByUser(requestor.getId());

        assertEquals(2, result.size());

        assertTrue(result.stream()
                .anyMatch(request ->
                        request.getId().equals(firstRequest.getId())));

        assertTrue(result.stream()
                .anyMatch(request ->
                        request.getId().equals(secondRequest.getId())));
    }

    @Test
    void getAllRequestsShouldReturnAllRequests() {
        ItemRequest firstRequest = createRequest(
                "Нужна дрель",
                requestor
        );

        ItemRequest secondRequest = createRequest(
                "Нужен молоток",
                owner
        );

        List<ItemRequestDto> result =
                itemRequestService.getAllRequests();

        assertEquals(2, result.size());

        assertTrue(result.stream()
                .anyMatch(request ->
                        request.getId().equals(firstRequest.getId())));

        assertTrue(result.stream()
                .anyMatch(request ->
                        request.getId().equals(secondRequest.getId())));
    }

    @Test
    void getRequestByIdShouldReturnRequestWithItems() {
        ItemRequest request = createRequest(
                "Нужна дрель",
                requestor
        );

        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);
        item.setOwner(owner.getId());
        item.setRequest(request);

        item = itemRepository.save(item);

        ItemRequestDto result = itemRequestService.getRequestById(
                request.getId(),
                requestor.getId()
        );

        assertEquals(request.getId(), result.getId());
        assertEquals("Нужна дрель", result.getDescription());

        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());

        assertEquals(
                item.getId(),
                result.getItems().get(0).getId()
        );
        assertEquals(
                "Дрель",
                result.getItems().get(0).getName()
        );
        assertEquals(
                request.getId(),
                result.getItems().get(0).getRequestId()
        );
    }

    private ItemRequest createRequest(
            String description,
            User requestor
    ) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        return itemRequestRepository.save(request);
    }
}
