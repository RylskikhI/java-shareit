package ru.practicum.shareit.item.controller;

import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.client.BaseClient;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit.server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getItemById(Long userId, Long id) {
        return get("/" + id, userId);
    }

    public ResponseEntity<Object> searchItems(Long userId, String text) {
        Map<String, Object> parameters = Map.of(
                "text", text
        );
        return get("/search?text={text}", userId, parameters);
    }

    public ResponseEntity<Object> getAllItems(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> addItem(ItemDto dto, Long userId) {
        return post("", userId, dto);
    }

    public ResponseEntity<Object> addComment(CommentDto dto, Long userId, Long id) {
        return post("/" + id + "/comment", userId, dto);
    }

    public ResponseEntity<Object> updateItem(ItemDto dto, Long userId, Long id) {
        return patch("/" + id, userId, dto);
    }

    public ResponseEntity<Object> deleteItemById(Long userId, Long id) {
        return delete("/" + id, userId);
    }
}
