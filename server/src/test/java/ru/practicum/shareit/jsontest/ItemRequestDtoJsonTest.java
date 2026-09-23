package ru.practicum.shareit.jsontest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void shouldSerializeItemRequestDto() throws Exception {
        ItemShortDto item = new ItemShortDto(
                10L,
                "Дрель",
                "Мощная дрель",
                true,
                1L
        );

        ItemRequestDto request = new ItemRequestDto(
                1L,
                "Нужна дрель",
                LocalDateTime.of(2026, 9, 23, 12, 30),
                List.of(item)
        );

        assertThat(json.write(request))
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(1);

        assertThat(json.write(request))
                .extractingJsonPathStringValue("$.description")
                .isEqualTo("Нужна дрель");

        assertThat(json.write(request))
                .extractingJsonPathStringValue("$.created")
                .isEqualTo("2026-09-23T12:30:00");

        assertThat(json.write(request))
                .extractingJsonPathNumberValue("$.items[0].id")
                .isEqualTo(10);

        assertThat(json.write(request))
                .extractingJsonPathStringValue("$.items[0].name")
                .isEqualTo("Дрель");
    }
}
