package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Collection<UserDto> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserDto updateUser(Long userId, User user) {
        return userRepository.update(userId, user);
    }

    @Override
    public UserDto getUserById(Long userId) {
        return userRepository.getUser(userId);
    }

    @Override
    public void deleteUser(long userId) {
        userRepository.deleteUser(userId);
    }
}
