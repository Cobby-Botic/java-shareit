package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class User {
    private Long id;
    @NotBlank
    private String name;
    @Email(message = "Некорректно указан email")
    @NotBlank
    private String email;
    private List<Review> reviews;
}
