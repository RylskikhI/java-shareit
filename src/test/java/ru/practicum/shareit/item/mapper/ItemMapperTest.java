package ru.practicum.shareit.item.mapper;

import java.util.Set;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.request.model.ItemRequest;
import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {
    private static final User OWNER = new User(1L, "John", "john@gmail.com");
    private static final User REQUESTOR = new User(2L, "Fred", "fred@gmail.com");
    private static final ItemRequest REQUEST = new ItemRequest(1L, "Circular saw", LocalDateTime.now(),
            REQUESTOR);
    private static final Item ITEM = Item.builder()
            .id(1L)
            .name("Saw")
            .description("Circular saw")
            .available(true)
            .owner(OWNER)
            .request(REQUEST)
            .build();
    private static final ItemDto DTO = ItemMapper.mapToItemDto(ITEM);
    private static final Booking LAST_BOOKING = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusDays(2),
            BookingStatus.WAITING, ITEM, new User(3L, "George", "george@gmail.com"));
    private static final Booking NEXT_BOOKING = new Booking(2L, LocalDateTime.now(), LocalDateTime.now().plusDays(5),
            BookingStatus.WAITING, ITEM, new User(4L, "Mike", "mike@gmail.com"));

    @ParameterizedTest
    @MethodSource("getRequest")
    void mapToItemDtoByRequest(ItemRequest request) {
        ItemDto dto = request == null ? ItemMapper.mapToItemDto(ITEM) : ItemMapper.mapToItemDto(ITEM, request);

        assertEquals(dto.getId(), ITEM.getId());
        assertEquals(dto.getName(), ITEM.getName());

        if (request == null) {
            assertNull(dto.getDescription());
            assertNull(dto.getAvailable());
            assertNull(dto.getRequestId());
        } else {
            assertEquals(dto.getDescription(), ITEM.getDescription());
            assertEquals(dto.getAvailable(), ITEM.getAvailable());
            assertEquals(dto.getRequestId(), ITEM.getRequest().getId());
        }
    }

    @ParameterizedTest
    @MethodSource("getBookings")
    void mapToItemDtoByBookings(Booking lastBooking, Booking nextBooking, Set<Comment> comments) {
        ItemDto dto = lastBooking == null && nextBooking == null ?
                ItemMapper.mapToItemDto(ITEM, comments) :
                ItemMapper.mapToItemDto(ITEM, lastBooking, nextBooking, comments);

        assertEquals(dto.getId(), ITEM.getId());
        assertEquals(dto.getName(), ITEM.getName());
        assertEquals(dto.getDescription(), ITEM.getDescription());
        assertEquals(dto.getAvailable(), ITEM.getAvailable());

        if (lastBooking == null && nextBooking == null) {
            assertNull(dto.getLastBooking());
            assertNull(dto.getNextBooking());
            assertEquals(0, dto.getComments().size());
        } else {
            assertNotNull(dto.getLastBooking());
            assertNotNull(dto.getNextBooking());
            assertEquals(0, dto.getComments().size());
        }
    }

    @ParameterizedTest
    @MethodSource("getRequest")
    void mapToItem(ItemRequest request) {
        Item item = ItemMapper.mapToItem(DTO, OWNER, request);

        assertEquals(item.getId(), DTO.getId());
        assertEquals(item.getName(), DTO.getName());
        assertEquals(item.getDescription(), DTO.getDescription());
        assertEquals(item.getAvailable(), DTO.getAvailable());
        assertEquals(item.getOwner(), OWNER);

        if (request == null) {
            assertNull(item.getRequest());
        } else {
            assertEquals(item.getRequest(), REQUEST);
        }
    }

    private static Stream<Arguments> getRequest() {
        return Stream.of(
                Arguments.of((ItemRequest) null),
                Arguments.of(REQUEST)
        );
    }

    private static Stream<Arguments> getBookings() {
        return Stream.of(
                Arguments.of(null, null, Set.of()),
                Arguments.of(LAST_BOOKING, NEXT_BOOKING, Set.of())
        );
    }
}
