package ru.practicum.shareit.item.dto;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemDtoMapper implements RowMapper<ItemDto> {

    @Override
    public ItemDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        Long owner = rs.getLong("owner");
        Boolean available = rs.getBoolean("available");

        return new ItemDto(id, name, description, available, owner);
    }
}
