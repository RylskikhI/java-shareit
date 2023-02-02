package ru.practicum.shareit.request;

import java.util.Set;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import static org.junit.jupiter.api.Assertions.*;

class ItemRequestMapperTest {
    private static final User REQUESTOR = new User(2L, "John", "john@gmail.com");
    private static final ItemRequest REQUEST = new ItemRequest(1L, "Circular saw", LocalDateTime.now(),
            REQUESTOR);
    private static final ItemRequestDto DTO = ItemRequestMapper.mapToItemRequestDto(REQUEST);

    @ParameterizedTest
    @MethodSource("getRequest")
    void mapToItemRequestDto(Set<Item> items) {
        ItemRequestDto dto = items == null ?
                ItemRequestMapper.mapToItemRequestDto(REQUEST) :
                ItemRequestMapper.mapToItemRequestDto(REQUEST, items);

        assertEquals(REQUEST.getId(), dto.getId());
        assertEquals(REQUEST.getDescription(), dto.getDescription());
        assertEquals(REQUEST.getCreated(), dto.getCreated());

        if (items == null) {
            assertNull(dto.getItems());
        } else {
            assertEquals(0, dto.getItems().size());
        }
    }

    @Test
    void mapToItemRequest() {
        ItemRequest request = ItemRequestMapper.mapToItemRequest(DTO, REQUESTOR);

        assertEquals(DTO.getId(), request.getId());
        assertEquals(DTO.getDescription(), request.getDescription());
        assertEquals(REQUESTOR, request.getRequestor());
    }

    private static Stream<Arguments> getRequest() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of(Set.of())
        );
    }
}
