package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void getItemByIdShouldReturnOk() throws Exception {
        Long userId = 1L;
        Long itemId = 10L;

        when(itemClient.getItemById(itemId, userId))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(itemClient).getItemById(itemId, userId);
    }

    @Test
    void getItemsShouldReturnOk() throws Exception {
        Long userId = 1L;

        when(itemClient.getAllItems(userId))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(itemClient).getAllItems(userId);
    }

    @Test
    void addItemShouldReturnOk() throws Exception {
        Long userId = 1L;

        ItemDto itemDto = new ItemDto(
                null,
                "Дрель",
                "Обычная дрель",
                null,
                true
        );

        when(itemClient.addItem(any(ItemDto.class), eq(userId)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        verify(itemClient).addItem(any(ItemDto.class), eq(userId));
    }

    @Test
    void addItemShouldReturnBadRequestWhenNameIsBlank() throws Exception {
        ItemDto itemDto = new ItemDto(
                null,
                "",
                "Обычная дрель",
                null,
                true
        );

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never())
                .addItem(any(ItemDto.class), anyLong());
    }

    @Test
    void addItemShouldReturnBadRequestWhenDescriptionIsBlank() throws Exception {
        ItemDto itemDto = new ItemDto(
                null,
                "Дрель",
                "",
                null,
                true
        );

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never())
                .addItem(any(ItemDto.class), anyLong());
    }

    @Test
    void addItemShouldReturnBadRequestWhenAvailableIsNull() throws Exception {
        ItemDto itemDto = new ItemDto(
                null,
                "Дрель",
                "Обычная дрель",
                null,
                null
        );

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never())
                .addItem(any(ItemDto.class), anyLong());
    }

    @Test
    void updateItemShouldReturnOk() throws Exception {
        Long userId = 1L;
        Long itemId = 10L;

        ItemDto itemDto = new ItemDto(
                null,
                "Новое название",
                null,
                null,
                null
        );

        when(itemClient.updateItem(
                any(ItemDto.class),
                eq(userId),
                eq(itemId)
        )).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        verify(itemClient).updateItem(
                any(ItemDto.class),
                eq(userId),
                eq(itemId)
        );
    }

    @Test
    void deleteItemShouldReturnOk() throws Exception {
        Long userId = 1L;
        Long itemId = 10L;

        when(itemClient.deleteItem(itemId, userId))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(itemClient).deleteItem(itemId, userId);
    }

    @Test
    void searchShouldReturnOk() throws Exception {
        Long userId = 1L;

        when(itemClient.search("дрель", userId))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", "дрель"))
                .andExpect(status().isOk());

        verify(itemClient).search("дрель", userId);
    }

    @Test
    void createCommentShouldReturnOk() throws Exception {
        Long userId = 1L;
        Long itemId = 10L;

        NewCommentDto commentDto = new NewCommentDto();
        commentDto.setText("Отличная дрель");

        when(itemClient.createComment(
                userId,
                itemId,
                commentDto
        )).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());

        verify(itemClient)
                .createComment(userId, itemId, commentDto);
    }

    @Test
    void createCommentShouldReturnBadRequestWhenTextIsBlank() throws Exception {
        NewCommentDto commentDto = new NewCommentDto();
        commentDto.setText("");

        mockMvc.perform(post("/items/{itemId}/comment", 10L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never())
                .createComment(anyLong(), anyLong(), any(NewCommentDto.class));
    }
}