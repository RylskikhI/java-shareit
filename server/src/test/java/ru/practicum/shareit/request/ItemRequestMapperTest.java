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

        assertEquals(dto.getId(), REQUEST.getId());
        assertEquals(dto.getDescription(), REQUEST.getDescription());
        assertEquals(dto.getCreated(), REQUEST.getCreated());

        if (items == null) {
            assertNull(dto.getItems());
        } else {
            assertEquals(dto.getItems().size(), 0);
        }
    }

    @Test
    void mapToItemRequest() {
        ItemRequest request = ItemRequestMapper.mapToItemRequest(DTO, REQUESTOR);

        assertEquals(request.getId(), DTO.getId());
        assertEquals(request.getDescription(), DTO.getDescription());
        assertEquals(request.getRequestor(), REQUESTOR);
    }

    private static Stream<Arguments> getRequest() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of(Set.of())
        );
    }
}
