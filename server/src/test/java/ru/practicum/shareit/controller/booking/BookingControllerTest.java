package ru.practicum.shareit.controller.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.service.booking.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void addBookingShouldReturnOk() throws Exception {
        Long userId = 1L;

        NewBookingDto bookingDto = new NewBookingDto();
        bookingDto.setItemId(10L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingService.addBooking(
                any(NewBookingDto.class),
                eq(userId)
        )).thenReturn(new BookingDto());

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk());

        verify(bookingService)
                .addBooking(any(NewBookingDto.class), eq(userId));
    }

    @Test
    void addBookingShouldReturnBadRequestWhenStartIsInPast() throws Exception {
        NewBookingDto bookingDto = new NewBookingDto();
        bookingDto.setItemId(10L);
        bookingDto.setStart(LocalDateTime.now().minusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());

        verify(bookingService, never())
                .addBooking(any(NewBookingDto.class), anyLong());
    }

    @Test
    void getBookingsByUserShouldReturnOk() throws Exception {
        Long userId = 1L;

        when(bookingService.getBookingsByUser(userId))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(bookingService).getBookingsByUser(userId);
    }

    @Test
    void getBookingShouldReturnOk() throws Exception {
        Long userId = 1L;
        Long bookingId = 10L;

        when(bookingService.getBooking(bookingId, userId))
                .thenReturn(new BookingDto());

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(bookingService).getBooking(bookingId, userId);
    }

    @Test
    void getBookingsByOwnerShouldReturnOk() throws Exception {
        Long userId = 1L;

        when(bookingService.getBookingsByOwner(userId))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(bookingService).getBookingsByOwner(userId);
    }

    @Test
    void updateBookingShouldReturnOk() throws Exception {
        Long userId = 1L;
        Long bookingId = 10L;

        when(bookingService.updateBooking(
                bookingId,
                userId,
                true
        )).thenReturn(new BookingDto());

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        verify(bookingService)
                .updateBooking(bookingId, userId, true);
    }
}
