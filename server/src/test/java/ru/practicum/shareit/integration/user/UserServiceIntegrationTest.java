package ru.practicum.shareit.integration.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void addUserShouldSaveUserToDatabase() {
        UserDto userDto = new UserDto();
        userDto.setName("Daniel");
        userDto.setEmail("daniel@test.ru");

        UserDto result = userService.addUser(userDto);

        assertNotNull(result.getId());
        assertEquals("Daniel", result.getName());
        assertEquals("daniel@test.ru", result.getEmail());

        User savedUser = userRepository.findById(result.getId()).orElseThrow();

        assertEquals("Daniel", savedUser.getName());
        assertEquals("daniel@test.ru", savedUser.getEmail());
    }

    @Test
    void getUserByIdShouldReturnUserFromDatabase() {
        User user = new User();
        user.setName("Daniel");
        user.setEmail("daniel@test.ru");

        User savedUser = userRepository.save(user);

        UserDto result = userService.getUserById(savedUser.getId());

        assertEquals(savedUser.getId(), result.getId());
        assertEquals("Daniel", result.getName());
        assertEquals("daniel@test.ru", result.getEmail());
    }

    @Test
    void getUsersShouldReturnUsersFromDatabase() {
        User firstUser = new User();
        firstUser.setName("Daniel");
        firstUser.setEmail("daniel@test.ru");

        User secondUser = new User();
        secondUser.setName("Alex");
        secondUser.setEmail("alex@test.ru");

        userRepository.save(firstUser);
        userRepository.save(secondUser);

        List<UserDto> result = userService.getUsers();

        assertEquals(2, result.size());
        assertTrue(result.stream()
                .anyMatch(user -> user.getEmail().equals("daniel@test.ru")));
        assertTrue(result.stream()
                .anyMatch(user -> user.getEmail().equals("alex@test.ru")));
    }

    @Test
    void updateUserShouldUpdateUserInDatabase() {
        User user = new User();
        user.setName("Daniel");
        user.setEmail("daniel@test.ru");

        User savedUser = userRepository.save(user);

        UserDto updateDto = new UserDto();
        updateDto.setName("New Daniel");

        UserDto result = userService.updateUser(
                updateDto,
                savedUser.getId()
        );

        assertEquals("New Daniel", result.getName());
        assertEquals("daniel@test.ru", result.getEmail());

        User updatedUser = userRepository
                .findById(savedUser.getId())
                .orElseThrow();

        assertEquals("New Daniel", updatedUser.getName());
        assertEquals("daniel@test.ru", updatedUser.getEmail());
    }

    @Test
    void deleteUserShouldDeleteUserFromDatabase() {
        User user = new User();
        user.setName("Daniel");
        user.setEmail("daniel@test.ru");

        User savedUser = userRepository.save(user);

        userService.deleteUser(savedUser.getId());

        assertFalse(userRepository.existsById(savedUser.getId()));
    }
}
