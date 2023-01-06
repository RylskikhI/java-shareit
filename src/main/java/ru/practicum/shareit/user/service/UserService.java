package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    /**
     * Создание пользователя.
     * @param userDto Entity.
     * @return UserDto.
     */
    UserDto saveUser(UserDto userDto);

    /**
     * Найти всех пользователей.
     * @return List<UserDto>.
     */
    List<UserDto> getAllUsers();

    /**
     * Обновить пользователя по id.
     * @param updatedUser Entity.
     * @param id User id.
     * @return User.
     */
    User updateUser(Long id, User updatedUser);

    /**
     * Поиск пользователя по id.
     * @param userId User id.
     * @return Optional<User>.
     */
    Optional<User> getUserById(Long userId);

    /**
     * Удалить пользователя по id.
     * @param userId User id.
     */
    void deleteUser(long userId);
}
