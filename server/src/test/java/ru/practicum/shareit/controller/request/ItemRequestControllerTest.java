package ru.practicum.shareit.controller.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void createNewRequestShouldReturnOk() throws Exception {
        NewItemRequestDto requestDto = new NewItemRequestDto();
        requestDto.setDescription("Нужна дрель");

        when(itemRequestService.createNewRequest(
                eq(1L), any(NewItemRequestDto.class)))
                .thenReturn(new ItemRequestDto());

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        verify(itemRequestService)
                .createNewRequest(eq(1L), any(NewItemRequestDto.class));
    }

    @Test
    void getRequestsByUserShouldReturnOk() throws Exception {
        when(itemRequestService.getRequestByUser(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(itemRequestService).getRequestByUser(1L);
    }

    @Test
    void getAllRequestsShouldReturnOk() throws Exception {
        when(itemRequestService.getAllRequests())
                .thenReturn(List.of());

        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isOk());

        verify(itemRequestService).getAllRequests();
    }

    @Test
    void getRequestByIdShouldReturnOk() throws Exception {
        when(itemRequestService.getRequestById(10L, 1L))
                .thenReturn(new ItemRequestDto());

        mockMvc.perform(get("/requests/{requestId}", 10L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(itemRequestService).getRequestById(10L, 1L);
    }
}
