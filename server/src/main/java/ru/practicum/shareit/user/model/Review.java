package ru.practicum.shareit.user.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Review {
    private Long id;
    private Long authorId;
    private int rating;
    private String text;
    private LocalDateTime timeOfCreation;
    private Long itemId;
}