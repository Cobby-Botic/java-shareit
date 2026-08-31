package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Review {
    private Long id;
    private Long authorId;
    @NotBlank
    private int rating;
    @NotBlank
    private String text;
    private LocalDateTime timeOfCreation;
    private Long itemId;
}