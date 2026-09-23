package ru.practicum.shareit.controller.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.service.item.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void getItemByIdShouldReturnOk() throws Exception {
        when(itemService.getItemById(10L, 1L))
                .thenReturn(new ItemDto());

        mockMvc.perform(get("/items/{itemId}", 10L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(itemService).getItemById(10L, 1L);
    }

    @Test
    void getItemsShouldReturnOk() throws Exception {
        when(itemService.getAllItems(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(itemService).getAllItems(1L);
    }

    @Test
    void updateItemShouldReturnOk() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Новое название");

        when(itemService.updateItem(
                any(ItemDto.class), eq(1L), eq(10L)))
                .thenReturn(new ItemDto());

        mockMvc.perform(patch("/items/{itemId}", 10L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        verify(itemService)
                .updateItem(any(ItemDto.class), eq(1L), eq(10L));
    }

    @Test
    void addItemShouldReturnOk() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Обычная дрель");
        itemDto.setAvailable(true);

        when(itemService.addItem(any(ItemDto.class), eq(1L)))
                .thenReturn(new ItemDto());

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        verify(itemService)
                .addItem(any(ItemDto.class), eq(1L));
    }

    @Test
    void createCommentShouldReturnOk() throws Exception {
        NewCommentDto commentDto = new NewCommentDto();
        commentDto.setText("Хорошая дрель");

        when(itemService.createComment(1L, 10L, "Хорошая дрель"))
                .thenReturn(new CommentDto());

        mockMvc.perform(post("/items/{itemId}/comment", 10L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());

        verify(itemService)
                .createComment(1L, 10L, "Хорошая дрель");
    }

    @Test
    void deleteItemShouldReturnOk() throws Exception {
        when(itemService.deleteItem(10L, 1L))
                .thenReturn(new ItemDto());

        mockMvc.perform(delete("/items/{itemId}", 10L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(itemService).deleteItem(10L, 1L);
    }

    @Test
    void searchShouldReturnOk() throws Exception {
        when(itemService.searchItem("дрель"))
                .thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "дрель"))
                .andExpect(status().isOk());

        verify(itemService).searchItem("дрель");
    }
}
