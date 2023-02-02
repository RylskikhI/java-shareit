package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.BookerDto;
import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {
    private static final User USER = new User(1L, "John", "john@gmail.com");
    private static final UserDto DTO = UserMapper.mapToUserDto(USER);

    @Test
    void mapToUserDto() {
        UserDto dto = UserMapper.mapToUserDto(USER);

        assertEquals(USER.getId(), dto.getId());
        assertEquals(USER.getName(), dto.getName());
        assertEquals(USER.getEmail(), dto.getEmail());
    }

    @Test
    void mapToBookerDto() {
        BookerDto dto = UserMapper.toBookerDto(USER);

        assertEquals(USER.getId(), dto.getId());
    }

    @Test
    void mapToUser() {
        User user = UserMapper.mapToUser(DTO);

        assertEquals(DTO.getId(), user.getId());
        assertEquals(DTO.getName(), user.getName());
        assertEquals(DTO.getEmail(), user.getEmail());
    }
}
