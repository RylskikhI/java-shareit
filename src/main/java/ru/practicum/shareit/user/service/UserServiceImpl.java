package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public UserDto saveUser(UserDto userDto) {
        if (userValidation(userDto)) {
            User user = userRepository.save(UserMapper.mapToUser(userDto));
            return UserMapper.mapToUserDto(user);
        } else {
            throw new ValidationException("Проверьте корректность введённых данных");
        }
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Запрошены все пользователи");
        try {
            List<User> users = userRepository.findAllCustom();
            if (users.isEmpty()) {
                log.warn("Список пользователей пуст");
                return Collections.emptyList();
            }
            return UserMapper.mapToUserDto(users);
        } catch (Exception e) {
            log.error("Ошибка при получении списка пользователей", e);
            throw e;
        }
    }

    @Transactional
    @Override
    public User updateUser(Long id, User updatedUser) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("Пользоветель не найден"));

        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null) {
            UserDto userDto = UserMapper.mapToUserDto(updatedUser);
            if (userValidation(userDto)) {
                user.setEmail(updatedUser.getEmail());
            } else {
                throw new DuplicateException("Не удалось обновить данные пользователя. "
                        + "Пользователь с таким email уже существует");
            }
        }
        return user;
    }

    @Transactional
    @Override
    public Optional<User> getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("Пользоветель не найден"));
        return Optional.ofNullable(user);
    }

    @Transactional
    @Override
    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
    }

    private boolean userValidation(UserDto userDto) {
        boolean isValidated = true;

        if (userDto.getEmail() == null) {
            isValidated = false;
            log.warn("Email не может быть null");
        }

        return isValidated;
    }
}
