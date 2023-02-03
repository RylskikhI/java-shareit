package ru.practicum.shareit.user;

import java.util.List;

import org.mockito.Mockito;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    private User user;
    private UserDto dto;
    private Gson gson;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    @BeforeEach
    void init() {
        user = new User(1L, "John", "john@gmail.com");
        dto = UserMapper.mapToUserDto(user);
        gson = new GsonBuilder()
                .serializeNulls()
                .create();
    }

    @AfterEach
    void tearDown() {
        user = null;
        dto = null;
        gson = null;
    }

    @Test
    @DisplayName("Send GET request /users/{id}")
    void getUserById() throws Exception {
        Mockito.when(userService.getUserById(user.getId())).thenReturn(dto);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber());

        Mockito.verify(userService, Mockito.times(1)).getUserById(user.getId());
    }

    @Test
    @DisplayName("Send GET request /users/{id}")
    void getUserByNotValidId() throws Exception {
        Mockito.when(userService.getUserById(user.getId())).thenThrow(UserNotFoundException.class);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(userService, Mockito.times(1)).getUserById(user.getId());
    }

    @Test
    @DisplayName("Send GET request /users")
    void getAllUsers() throws Exception {
        Mockito.when(userService.getAllUsers()).thenReturn(List.of(dto));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());

        Mockito.verify(userService, Mockito.times(1)).getAllUsers();
    }

    @Test
    @DisplayName("Send POST request /users")
    void saveNewUser() throws Exception {
        Mockito.when(userService.saveUser(Mockito.any())).thenReturn(dto);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(dto)))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1)).saveUser(Mockito.any());
    }

    @Test
    @DisplayName("Send PATCH request /users/{id}")
    void updateUser() throws Exception {
        dto.setName("Mike");
        Mockito.when(userService.updateUser(Mockito.anyLong(), Mockito.any())).thenReturn(dto);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(dto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Mike"));

        Mockito.verify(userService, Mockito.times(1)).updateUser(Mockito.anyLong(), Mockito.any());
    }

    @Test
    @DisplayName("Send DELETE request users/{id}")
    void deleteUser() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1)).deleteUser(user.getId());
    }
}
