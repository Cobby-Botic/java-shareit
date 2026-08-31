package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.user.model.Review;

import java.util.List;

@Data
@AllArgsConstructor
public class UserDto {
    private Long id;

    @NotNull
    @NotBlank
    private String name;

    @Email
    @NotNull
    @NotBlank
    private String email;
    private List<Review> reviews;

    public UserDto(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
