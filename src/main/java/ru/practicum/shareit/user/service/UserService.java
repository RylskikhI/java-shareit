package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto saveUser(UserDto userDto);

    List<UserDto> getAllUsers();

    User updateUser(Long id, User updatedUser);

    Optional<User> getUserById(Long userId);

    void deleteUser(long userId);
}
