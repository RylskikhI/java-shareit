package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

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
        List<User> users = userRepository.findAllCustom();
        return UserMapper.mapToUserDto(users);
    }

    @Transactional
    @Override
    public UserDto updateUser(Long id, UserDto updatedUser) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("Пользователь не найден"));

        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null) {
            if (userValidation(updatedUser)) {
                user.setEmail(updatedUser.getEmail());
            } else {
                throw new SecurityException("Не удалось обновить данные пользователя. "
                        + "Пользователь с таким email уже существует");
            }
        }
        return UserMapper.mapToUserDto(user);
    }

    @Transactional
    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("Пользователь не найден"));
        return UserMapper.mapToUserDto(user);
    }

    @Transactional
    @Override
    public void deleteUser(long userId) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("Пользователь с id=%d не найден!", userId))
        );
        userRepository.deleteById(userId);
    }

    private boolean userValidation(UserDto userDto) {
        boolean isValidated = userDto.getEmail() != null;

        return isValidated;
    }
}
