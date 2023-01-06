package ru.practicum.shareit.user.mapper;

import ru.practicum.shareit.user.dto.BookerDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class UserMapper {

    public static UserDto mapToUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static User mapToUser(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        return user;
    }

    public static List<UserDto> mapToUserDto(Iterable<User> users) {
        List<UserDto> result = new ArrayList<>();

        for (User user : users) {
            result.add(mapToUserDto(user));
        }

        return result;
    }

    public static BookerDto toBookerDto(@NotNull User user) {
        return new BookerDto(
                user.getId()
        );
    }
}
