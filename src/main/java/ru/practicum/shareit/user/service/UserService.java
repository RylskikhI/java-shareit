package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

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
     * @return UserDto.
     */
    UserDto updateUser(Long id, UserDto updatedUser);

    /**
     * Поиск пользователя по id.
     * @param userId User id.
     * @return UserDto.
     */
    UserDto getUserById(Long userId);

    /**
     * Удалить пользователя по id.
     * @param userId User id.
     */
    void deleteUser(long userId);
}
