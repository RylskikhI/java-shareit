package ru.practicum.shareit.user;

import java.util.List;
import java.util.Optional;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.ParameterizedTest;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private User user;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void init() {
        user = new User(1L, "Susan", "susan@gmail.com");
    }

    @AfterEach
    void tearDown() {
        user = null;
    }

    @Test
    void getUserById() {
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDto dto = userService.getUserById(user.getId());

        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getName(), dto.getName());
        assertEquals(user.getEmail(), dto.getEmail());

        Mockito.verify(userRepository, Mockito.times(1)).findById(user.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void getUserByNotValidId(Long userId) {
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(userId);
        });

        String expectedMessage = "Пользователь не найден";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void getAllUsers() {
        Mockito.when(userRepository.findAllCustom()).thenReturn(List.of(user));

        List<UserDto> users = userService.getAllUsers();

        assertEquals(1, users.size());

        Mockito.verify(userRepository, Mockito.times(1)).findAllCustom();
    }

    @Test
    void saveNewUser() {
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);

        UserDto dto = UserMapper.mapToUserDto(user);
        UserDto savedDto = userService.saveUser(dto);

        assertEquals(dto.getId(), savedDto.getId());
        assertEquals(dto.getName(), savedDto.getName());
        assertEquals(dto.getEmail(), savedDto.getEmail());

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void updateUser() {
        User newUser = new User(user.getId(), "Mike", "mike@gmail.com");
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDto dto = UserMapper.mapToUserDto(newUser);
        UserDto savedUser = userService.updateUser(newUser.getId(), dto);

        assertEquals(newUser.getId(), savedUser.getId());
        assertEquals(newUser.getName(), savedUser.getName());
        assertEquals(newUser.getEmail(), savedUser.getEmail());

        Mockito.verify(userRepository, Mockito.times(1)).findById(user.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void updateUserByNotValidId(Long userId) {
        User newUser = new User(userId, "Mike", "mike@gmail.com");
        UserDto dto = UserMapper.mapToUserDto(newUser);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(userId, dto);
        });

        String expectedMessage = "Пользователь не найден";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void deleteUser() {
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.deleteUser(user.getId());

        Mockito.verify(userRepository, Mockito.times(1)).findById(user.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void deleteUserByNotValidId(Long userId) {
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(userId);
        });

        String expectedMessage = String.format("Пользователь с id=%d не найден!", userId);
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }
}
