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

        assertEquals(dto.getId(), USER.getId());
        assertEquals(dto.getName(), USER.getName());
        assertEquals(dto.getEmail(), USER.getEmail());
    }

    @Test
    void mapToBookerDto() {
        BookerDto dto = UserMapper.toBookerDto(USER);

        assertEquals(dto.getId(), USER.getId());
    }

    @Test
    void mapToUser() {
        User user = UserMapper.mapToUser(DTO);

        assertEquals(user.getId(), DTO.getId());
        assertEquals(user.getName(), DTO.getName());
        assertEquals(user.getEmail(), DTO.getEmail());
    }
}
