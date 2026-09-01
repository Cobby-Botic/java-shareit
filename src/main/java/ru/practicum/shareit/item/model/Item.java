package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Item {
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private Long owner;

    @NotNull
    private Boolean available;
}
