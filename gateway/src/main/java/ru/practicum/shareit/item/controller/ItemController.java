package ru.practicum.shareit.item.controller;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.CommentDto;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping("{id}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        log.info("Send get request /items/{}", id);
        return itemClient.getItemById(userId, id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(value = "text") String text) {
        log.info("Send get request /items/search?text={}", text);
        return text.isBlank() ? ResponseEntity.ok(Collections.emptyList()) : itemClient.searchItems(userId, text);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Send get request /items");
        return itemClient.getAllItems(userId);
    }

    @PostMapping
    public ResponseEntity<Object> addItem(@Valid @RequestBody ItemDto itemDto,
                                       @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Send post request /items");
        return itemClient.addItem(itemDto, userId);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Object> addComment(@Valid @RequestBody CommentDto commentDto,
                                              @RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long id) {
        log.info("Send post request /items/{}/comment", id);
        return itemClient.addComment(commentDto, userId, id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateItem(@RequestBody ItemDto itemDto,
                                         @RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long id) {
        log.info("Send patch request /items/{}", id);
        return itemClient.updateItem(itemDto, userId, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long id) {
        log.info("Send delete request /items/{}", id);
        return itemClient.deleteItemById(userId, id);
    }
}
