package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
@Slf4j
public class UserRepositoryImpl implements UserRepository{

    private final Map<Long, UserDto> users = new HashMap<>();

    private long id = 0;

    @Override
    public UserDto save(User user) {
        if (userValidation(user)) {
            user.setId(++id);
            users.put(user.getId(), UserMapper.toUserDto(user));
        } else {
            throw new ValidationException("Проверьте корректность введённых данных");
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public Collection<UserDto> findAll() {
        return users.values();
    }

    @Override
    public UserDto update(Long userId, User user) {
        UserDto userForUpdate = users.get(userId);
        if (user.getName() != null) {
            userForUpdate.setName(user.getName());
        }
        if (user.getEmail() != null) {
            if (userValidation(user)) {
                userForUpdate.setEmail(user.getEmail());
            } else {
                throw new DuplicateException("Не удалось обновить данные пользователя. "
                        + "Пользователь с таким email уже существует");
            }
        }
        return userForUpdate;
    }

    @Override
    public UserDto getUser(Long userId) {
        log.info("Запрошен пользователь с id " + userId);
        return users.get(userId);
    }

    @Override
    public void deleteUser(long userId) {
        log.info("Запрошено уделение пользователя с id " + userId);
        if (users.containsKey(userId)) {
            users.remove(userId);
        } else {
            throw new UserNotFoundException("Пользователь с id " + userId + " на найден");
        }
    }

    private boolean userValidation(User user) {
        boolean isValidated = true;

        if(user.getEmail() == null) {
            isValidated = false;
            log.warn("Email не может быть null");
        }

        if (users.entrySet()
                .stream()
                .anyMatch(t -> t.getValue().getEmail().equals(user.getEmail()))) {
            throw new DuplicateException("Email уже занят");
        }

        return isValidated;
    }
}
