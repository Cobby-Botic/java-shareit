package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    ItemRequestServiceImpl itemRequestService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Даня");
        user.setEmail("danya@mail.ru");
    }

    @Test
    void createNewRequestShouldCreateRequest() {
        NewItemRequestDto newRequest = new NewItemRequestDto();

        newRequest.setDescription("Нужна дрель");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(itemRequestRepository.save(any(ItemRequest.class)))
                .thenAnswer(invocation -> {
                    ItemRequest request = invocation.getArgument(0);
                    request.setId(1L);
                    return request;
                });

        ItemRequestDto result =
                itemRequestService.createNewRequest(1L, newRequest);

        assertEquals(1L, result.getId());
        assertEquals("Нужна дрель", result.getDescription());
    }

    @Test
    void getRequestByUserShouldReturnRequest() {
        
    }
}
