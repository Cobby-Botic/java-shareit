package ru.practicum.shareit.jsontest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void shouldSerializeBookingDto() throws Exception {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(
                LocalDateTime.of(2026, 9, 23, 15, 30)
        );
        bookingDto.setEnd(
                LocalDateTime.of(2026, 9, 24, 18, 45)
        );
        bookingDto.setStatus(BookingStatus.APPROVED);

        assertThat(json.write(bookingDto))
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(1);

        assertThat(json.write(bookingDto))
                .extractingJsonPathStringValue("$.start")
                .isEqualTo("2026-09-23T15:30:00");

        assertThat(json.write(bookingDto))
                .extractingJsonPathStringValue("$.end")
                .isEqualTo("2026-09-24T18:45:00");

        assertThat(json.write(bookingDto))
                .extractingJsonPathStringValue("$.status")
                .isEqualTo("APPROVED");
    }

    @Test
    void shouldDeserializeBookingDto() throws Exception {
        String content = "{"
                + "\"id\": 1,"
                + "\"start\": \"2026-09-23T15:30:00\","
                + "\"end\": \"2026-09-24T18:45:00\","
                + "\"status\": \"WAITING\""
                + "}";

        BookingDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStart())
                .isEqualTo(LocalDateTime.of(2026, 9, 23, 15, 30));
        assertThat(result.getEnd())
                .isEqualTo(LocalDateTime.of(2026, 9, 24, 18, 45));
        assertThat(result.getStatus())
                .isEqualTo(BookingStatus.WAITING);
    }
}
