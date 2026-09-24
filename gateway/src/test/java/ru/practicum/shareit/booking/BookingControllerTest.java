package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void getBookingsShouldReturnOk() throws Exception {
        Long userId = 1L;

        when(bookingClient.getBookings(
                userId,
                BookingState.ALL,
                0,
                10
        )).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(bookingClient).getBookings(
                userId,
                BookingState.ALL,
                0,
                10
        );
    }

    @Test
    void getBookingsShouldReturnBadRequestWhenFromIsNegative() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .getBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void getBookingsShouldReturnBadRequestWhenSizeIsNotPositive() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .getBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void getBookingsShouldReturnErrorWhenStateIsUnknown() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .getBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void getBookingShouldReturnOk() throws Exception {
        Long userId = 1L;
        Long bookingId = 10L;

        when(bookingClient.getBooking(userId, bookingId))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(bookingClient)
                .getBooking(userId, bookingId);
    }

    @Test
    void getBookingsByOwnerShouldReturnOk() throws Exception {
        Long userId = 1L;

        when(bookingClient.getBookingsByOwner(userId))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(bookingClient)
                .getBookingsByOwner(userId);
    }

    @Test
    void bookItemShouldReturnOk() throws Exception {
        Long userId = 1L;

        BookItemRequestDto requestDto = new BookItemRequestDto(
                10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        when(bookingClient.bookItem(
                eq(userId),
                any(BookItemRequestDto.class)
        )).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        verify(bookingClient).bookItem(
                eq(userId),
                any(BookItemRequestDto.class)
        );
    }

    @Test
    void bookItemShouldReturnBadRequestWhenStartIsInPast() throws Exception {
        Long userId = 1L;

        BookItemRequestDto requestDto = new BookItemRequestDto(
                10L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .bookItem(anyLong(), any(BookItemRequestDto.class));
    }

    @Test
    void bookItemShouldReturnBadRequestWhenEndIsInPast() throws Exception {
        Long userId = 1L;

        BookItemRequestDto requestDto = new BookItemRequestDto(
                10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().minusDays(1)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .bookItem(anyLong(), any(BookItemRequestDto.class));
    }

    @Test
    void bookItemShouldReturnBadRequestWhenStartIsNull() throws Exception {
        Long userId = 1L;

        BookItemRequestDto requestDto = new BookItemRequestDto(
                10L,
                null,
                LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .bookItem(anyLong(), any(BookItemRequestDto.class));
    }

    @Test
    void bookItemShouldReturnBadRequestWhenStartIsAfterEnd() throws Exception {
        Long userId = 1L;

        BookItemRequestDto requestDto = new BookItemRequestDto(
                10L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .bookItem(anyLong(), any(BookItemRequestDto.class));
    }
}