package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestClient requestClient;

    @Test
    void createNewRequestShouldReturnOk() throws Exception {
        Long userId = 1L;

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");

        when(requestClient.createNewRequest(
                eq(userId),
                any(ItemRequestDto.class)
        )).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        verify(requestClient)
                .createNewRequest(eq(userId), any(ItemRequestDto.class));
    }

    @Test
    void createNewRequestShouldReturnBadRequestWhenDescriptionIsBlank()
            throws Exception {

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("");

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(requestClient, never())
                .createNewRequest(any(), any(ItemRequestDto.class));
    }

    @Test
    void getRequestsByUserShouldReturnOk() throws Exception {
        Long userId = 1L;

        when(requestClient.getRequestByUser(userId))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(requestClient).getRequestByUser(userId);
    }

    @Test
    void getAllRequestsShouldReturnOk() throws Exception {
        when(requestClient.getAllRequests())
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isOk());

        verify(requestClient).getAllRequests();
    }

    @Test
    void getRequestByIdShouldReturnOk() throws Exception {
        Long userId = 1L;
        Long requestId = 10L;

        when(requestClient.getRequestById(requestId, userId))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(requestClient)
                .getRequestById(requestId, userId);
    }
}