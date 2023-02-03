package ru.practicum.shareit.user;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.TypedQuery;
import javax.persistence.EntityManager;

import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbTest {
    private User user;
    private final EntityManager em;
    private final UserService userService;

    @BeforeEach
    void init() {
        user = new User(null, "John", "john@gmail.com");
    }

    @AfterEach
    void tearDown() {
        user = null;
    }

    @Test
    void getUserById() {
        UserDto dto = makeUser(user);
        TypedQuery<Long> query = em.createQuery("select u.id from User as u where u.id = :id", Long.class);
        Long id = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        dto = userService.getUserById(id);

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getName()).isEqualTo(user.getName());
        assertThat(dto.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void getAllUsers() {
        makeUsers();
        TypedQuery<User> query = em.createQuery("select u from User as u", User.class);
        List<User> result = query.getResultList();
        List<UserDto> users = userService.getAllUsers();

        assertThat(result.size()).isEqualTo(users.size());
    }

    @Test
    void saveNewUser() {
        UserDto dto = makeUser(user);
        TypedQuery<User> query = em.createQuery("select u from User as u where u.email = :email", User.class);
        user = query
                .setParameter("email", dto.getEmail())
                .getSingleResult();

        assertThat(user.getId()).isNotNull();
        assertThat(user.getName()).isEqualTo(dto.getName());
        assertThat(user.getEmail()).isEqualTo(dto.getEmail());
    }

    @Test
    void saveNewUserByEmailExists() {
        makeUser(user);
        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> {
            makeUser(user);
        });

        assertNotNull(exception.getMessage());
    }

    @Test
    void updateUser() {
        UserDto dto = makeUser(user);
        TypedQuery<User> query = em.createQuery("select u from User as u where u.id = :id", User.class);
        user = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        User newUser = new User(user.getId(), "Mike", "mike@yahoo.com");
        dto = UserMapper.mapToUserDto(newUser);
        dto = userService.updateUser(dto.getId(), dto);

        assertThat(newUser.getId()).isNotNull();
        assertThat(newUser.getName()).isEqualTo(dto.getName());
        assertThat(newUser.getEmail()).isEqualTo(dto.getEmail());
    }

    @Test
    void deleteUser() {
        UserDto dto = makeUser(user);
        TypedQuery<Long> query = em.createQuery("select u.id from User as u where u.id = :id", Long.class);
        Long id = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        userService.deleteUser(id);
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(id);
        });

        String expectedMessage = "Пользователь не найден";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    private UserDto makeUser(User user) {
        UserDto dto = UserMapper.mapToUserDto(user);
        return userService.saveUser(dto);
    }

    private void makeUsers() {
        userService.saveUser(new UserDto(null, "Alex", "alex@gmail.com"));
        userService.saveUser(new UserDto(null, "Fred", "fred@gmail.com"));
        userService.saveUser(new UserDto(null, "Harry", "harry@gmail.com"));
    }
}
