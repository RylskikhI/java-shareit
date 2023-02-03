package ru.practicum.shareit.request.controller;

import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.client.BaseClient;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit.server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getRequestById(Long userId, Long id) {
        return get("/" + id, userId);
    }

    public ResponseEntity<Object> getRequestsMadeByOwner(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getRequestsMadeByOthers(Long userId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("/all?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> addNewRequest(ItemRequestDto dto, Long userId) {
        return post("", userId, dto);
    }

    public ResponseEntity<Object> deleteById(Long userId, Long id) {
        return delete("/" + id, userId);
    }
}
