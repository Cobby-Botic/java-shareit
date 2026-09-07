package ru.practicum.shareit.user.model;

import lombok.Data;

import java.util.List;

@Data
public class User {
    private Long id;
    private String name;
    private String email;
    private List<Review> reviews;
}
