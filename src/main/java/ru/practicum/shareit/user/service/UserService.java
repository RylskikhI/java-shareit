package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {
    UserDto saveUser(User user);
    Collection<UserDto> getAllUsers();
    UserDto updateUser(Long userId, User user);
    UserDto getUserById(Long userId);
    void deleteUser(long userId);
}
