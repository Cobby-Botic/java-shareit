package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.Review;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserRepository {

    UserDto getUserById(Long id);

    List<UserDto> getUsers();

    UserDto addUser(UserDto user);

    UserDto updateUser(UserDto userDto, Long userId);

    void deleteUser(Long userId);

    UserDto addReviewToUser(Review review, Long userId, Long sharedUser);
}