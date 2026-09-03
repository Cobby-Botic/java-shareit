package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailDuplicateException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.Review;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserRepository implements UserRepository {

    private static Long userId = 1L;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public UserDto getUserById(Long id) {
        User user = findUser(id);
        log.info("Поиск user: " + user);

        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getUsers() {
        List<UserDto> usersDto = new ArrayList<>();
        users.values()
                .forEach(user -> usersDto.add(UserMapper.toUserDto(user)));

        return usersDto;
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        checkOnExist(user);
        checkDuplicateEmail(user, user.getId());
        user.setId(userId++);
        users.put(user.getId(), user);
        log.info("User: " + user.getId() + " добавлен");

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long userId) {
        User user = UserMapper.toUser(userDto);
        User currentUser = findUser(userId);

        if (user.getName() != null) {
            currentUser.setName(user.getName());
        }

        if (user.getEmail() != null) {
            checkDuplicateEmail(user, userId);
            currentUser.setEmail(user.getEmail());
        }
        return UserMapper.toUserDto(currentUser);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = findUser(userId);
        users.remove(userId);
        log.info("User: " + userId + " удален");
    }

    @Override
    public UserDto addReviewToUser(Review review, Long userId, Long sharedUser) {
        User user = findUser(userId);
        user.getReviews().add(review);
        log.info("Отзыв: " + review.getId() + " для User: " + userId);

        return UserMapper.toUserDto(user);
    }

    private void checkDuplicateEmail(User user, Long userId) {
        users.values().stream()
                        .filter(currentUser -> currentUser.getEmail().equals(user.getEmail()))
                                .forEach(currentUser -> {
                                    if (!currentUser.getId().equals(userId)) {
                                        throw new EmailDuplicateException("User с такой почтой уже существует");
                                    }
                                });
    }

    private void checkOnExist(User user) {
        if (users.containsKey(user.getId())) {
            throw new AlreadyExistsException("User с id: " + user.getId() + " уже существует");
        }
    }

    private User findUser(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("User с id " + id + " не найден");
        }
        return users.get(id);
    }
}