package ru.practicum.shareit.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

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
    private UserService userService;

    @Test
    void getUserByIdShouldReturnOk() throws Exception {
        when(userService.getUserById(1L))
                .thenReturn(new UserDto());

        mockMvc.perform(get("/users/{userId}", 1L))
                .andExpect(status().isOk());

        verify(userService).getUserById(1L);
    }

    @Test
    void getUsersShouldReturnOk() throws Exception {
        when(userService.getUsers())
                .thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());

        verify(userService).getUsers();
    }

    @Test
    void addUserShouldReturnOk() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Daniel");
        userDto.setEmail("daniel@test.ru");

        when(userService.addUser(any(UserDto.class)))
                .thenReturn(new UserDto());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        verify(userService).addUser(any(UserDto.class));
    }

    @Test
    void updateUserShouldReturnOk() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("New name");

        when(userService.updateUser(
                any(UserDto.class), eq(1L)))
                .thenReturn(new UserDto());

        mockMvc.perform(patch("/users/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        verify(userService)
                .updateUser(any(UserDto.class), eq(1L));
    }

    @Test
    void deleteUserShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/users/{userId}", 1L))
                .andExpect(status().isOk());

        verify(userService).deleteUser(1L);
    }
}