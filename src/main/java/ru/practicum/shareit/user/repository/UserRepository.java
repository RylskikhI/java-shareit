package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository {
    UserDto save(User user);
    Collection<UserDto> findAll();
    UserDto update(Long userId, User user);
    UserDto getUser(Long userId);
    void deleteUser(long userId);
}
