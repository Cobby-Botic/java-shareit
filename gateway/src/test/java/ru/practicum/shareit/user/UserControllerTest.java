package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    void getUserByIdShouldReturnOk() throws Exception {
        Long userId = 1L;

        when(userClient.getUserById(userId))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userClient).getUserById(userId);
    }

    @Test
    void getUsersShouldReturnOk() throws Exception {
        when(userClient.getUsers())
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());

        verify(userClient).getUsers();
    }

    @Test
    void addUserShouldReturnOk() throws Exception {
        UserDto userDto = new UserDto(
                null,
                "Daniel",
                "daniel@test.ru"
        );

        when(userClient.addUser(any(UserDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        verify(userClient).addUser(any(UserDto.class));
    }

    @Test
    void addUserShouldReturnBadRequestWhenNameIsBlank() throws Exception {
        UserDto userDto = new UserDto(
                null,
                "",
                "daniel@test.ru"
        );

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());

        verify(userClient, never())
                .addUser(any(UserDto.class));
    }

    @Test
    void addUserShouldReturnBadRequestWhenEmailIsBlank() throws Exception {
        UserDto userDto = new UserDto(
                null,
                "Daniel",
                ""
        );

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());

        verify(userClient, never())
                .addUser(any(UserDto.class));
    }

    @Test
    void addUserShouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        UserDto userDto = new UserDto(
                null,
                "Daniel",
                "not-email"
        );

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());

        verify(userClient, never())
                .addUser(any(UserDto.class));
    }

    @Test
    void updateUserShouldReturnOk() throws Exception {
        Long userId = 1L;

        UserDto userDto = new UserDto(
                null,
                null,
                "new@test.ru"
        );

        when(userClient.updateUser(
                any(UserDto.class),
                eq(userId)
        )).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        verify(userClient)
                .updateUser(any(UserDto.class), eq(userId));
    }

    @Test
    void deleteUserShouldReturnOk() throws Exception {
        Long userId = 1L;

        when(userClient.deleteUser(userId))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userClient).deleteUser(userId);
    }
}