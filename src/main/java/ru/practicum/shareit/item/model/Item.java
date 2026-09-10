package ru.practicum.shareit.item.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "items")
public class Item {
    @Id
    private Long id;
    private String name;
    private String description;
    private Long owner;
    private Boolean available;
}
