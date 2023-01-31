package ru.practicum.shareit.user.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.user.dto.UserDto;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserClient userClient;

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable Long id) {
        log.info("Send get request /users/{}", id);
        return userClient.findById(id);
    }

    @GetMapping
    public ResponseEntity<Object> findAll() {
        log.info("Send get request /users");
        return userClient.findAll();
    }

    @PostMapping
    public ResponseEntity<Object> save(@Valid @RequestBody UserDto userDto) {
        log.info("Send post request /users");
        return userClient.save(userDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@Valid @RequestBody UserDto userDto, @PathVariable Long id) {
        log.info("Send patch request /users/{}", id);
        return userClient.update(userDto, id);
    }

    @DeleteMapping({"/{id}"})
    public ResponseEntity<Object> deleteById(@PathVariable Long id) {
        log.info("Send delete request /users/{}", id);
        return userClient.deleteById(id);
    }
}
