package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailDuplicateException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserByIdShouldReturnUser() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("Daniel");
        user.setEmail("daniel@test.ru");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(userId);

        assertEquals(userId, result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void getUserByIdShouldThrowWhenUserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getUserById(userId)
        );

        assertEquals("Пользователя с ID = " + userId + " не существует", exception.getMessage());
    }

    @Test
    void getUsersShouldReturnUsers() {
        User firstUser = new User();
        firstUser.setId(1L);
        firstUser.setName("Daniel");
        firstUser.setEmail("daniel@test.ru");

        User secondUser = new User();
        secondUser.setId(2L);
        secondUser.setName("Alex");
        secondUser.setEmail("alex@test.ru");

        when(userRepository.findAll())
                .thenReturn(List.of(firstUser, secondUser));

        List<UserDto> result = userService.getUsers();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void getUsersShouldReturnEmptyList() {
        when(userService.getUsers())
                .thenReturn(List.of());

        List<UserDto> result = userService.getUsers();
        assertTrue(result.isEmpty());
    }

    @Test
    void addUserShouldSaveNewUser() {
        UserDto userDto = new UserDto(
                null,
                "Daniel",
                "daniel@test.ru"
        );

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Daniel");
        savedUser.setEmail("daniel@test.ru");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserDto result = userService.addUser(userDto);

        assertEquals(1L, result.getId());
        assertEquals("Daniel", result.getName());
        assertEquals("daniel@test.ru", result.getEmail());
    }

    @Test
    void addUserShouldThrowWhenEmailIsDuplicate() {
        UserDto userDto = new UserDto(
                null,
                "Daniel",
                "daniel@test.ru"
        );

        when(userRepository.save(any(User.class)))
                .thenThrow(DataIntegrityViolationException.class);

        EmailDuplicateException exception = assertThrows(
                EmailDuplicateException.class,
                () -> userService.addUser(userDto)
                );

        assertEquals("user с email " + userDto.getEmail() + " уже создан", exception.getMessage());
    }

    @Test
    void updateUserShouldUpdateUser() {
        Long userId = 1L;

        User currentUser = new User();
        currentUser.setId(userId);
        currentUser.setName("Daniel");
        currentUser.setEmail("daniel@test.ru");

        UserDto updateDto = new UserDto(
                null,
                "Danya",
                "new@test.ru"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(currentUser));

        when(userRepository.existsByEmailAndIdNot("new@test.ru", userId))
                .thenReturn(false);

        UserDto result = userService.updateUser(updateDto, userId);

        assertEquals(userId, result.getId());
        assertEquals("Danya", result.getName());
        assertEquals("new@test.ru", result.getEmail());
    }

    @Test
    void updateUserShouldUpdateWhenEmailBelongsToSameUser() {
        Long userId = 1L;

        User currentUser = new User();
        currentUser.setId(userId);
        currentUser.setName("Daniel");
        currentUser.setEmail("daniel@test.ru");

        UserDto updateDto = new UserDto(
                null,
                "Danya",
                "daniel@test.ru"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(currentUser));

        when(userRepository.existsByEmailAndIdNot("daniel@test.ru", userId))
                .thenReturn(false);

        UserDto result = userService.updateUser(updateDto, userId);

        assertEquals(userId, result.getId());
        assertEquals("Danya", result.getName());
        assertEquals("daniel@test.ru", result.getEmail());
    }

    @Test
    void updateUserShouldThrowWhenEmailAlreadyExists() {
        Long userId = 1L;

        User currentUser = new User();
        currentUser.setId(userId);
        currentUser.setName("Daniel");
        currentUser.setEmail("daniel@test.ru");

        UserDto updateDto = new UserDto(
                null,
                "Daniel",
                "alex@test.ru"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(currentUser));

        when(userRepository.existsByEmailAndIdNot("alex@test.ru", userId))
                .thenReturn(true);

        EmailDuplicateException exception = assertThrows(
                EmailDuplicateException.class,
                () -> userService.updateUser(updateDto, userId)
        );

        assertEquals(
                "Пользователь с почтой alex@test.ru уже зарегистрирован в системе",
                exception.getMessage()
        );
    }

    @Test
    void updateUserShouldThrowWhenUserNotFound() {
        Long userId = 1L;

        UserDto updateDto = new UserDto(
                null,
                "Danya",
                "daniel@test.ru"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.updateUser(updateDto, userId)
        );

        assertEquals(
                "User с ID " + userId + " не найден",
                exception.getMessage()
        );

        verify(userRepository, never())
                .existsByEmailAndIdNot(anyString(), anyLong());
    }

    @Test
    void deleteUserShouldDeleteUser() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setName("Daniel");
        user.setEmail("daniel@test.ru");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserShouldThrowWhenUserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.deleteUser(userId)
        );

        assertEquals(
                "User с id " + userId + " не найден",
                exception.getMessage()
        );

        verify(userRepository, never())
                .delete(any(User.class));
    }

}
