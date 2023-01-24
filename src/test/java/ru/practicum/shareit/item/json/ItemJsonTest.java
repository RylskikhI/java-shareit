package ru.practicum.shareit.item.json;

import java.io.IOException;
import java.util.Set;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.utils.LocalDateTimeAdapter;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemJsonTest {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .serializeNulls()
            .create();
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
    private static final Booking LAST_BOOKING = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusDays(2),
            BookingStatus.WAITING, ITEM, new User(3L, "George", "george@gmail.com"));
    private static final Booking NEXT_BOOKING = new Booking(2L, LocalDateTime.now(), LocalDateTime.now().plusDays(5),
            BookingStatus.WAITING, ITEM, new User(4L, "Mike", "mike@gmail.com"));
    @Autowired
    private JacksonTester<ItemDto> testerDto;

    @ParameterizedTest
    @MethodSource("getItemDto")
    void itemDtoSerialize(ItemDto dto) throws IOException {
        JsonContent<ItemDto> json = testerDto.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isNotNull();
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo(dto.getName());

        if (dto.getRequestId() != null) {
            assertThat(json).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
            assertThat(json).extractingJsonPathBooleanValue("$.available").isEqualTo(dto.getAvailable());
            assertThat(json).extractingJsonPathNumberValue("$.requestId").isNotNull();
        }
        if (dto.getLastBooking() != null || dto.getNextBooking() != null) {
            assertThat(json).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
            assertThat(json).extractingJsonPathBooleanValue("$.available").isEqualTo(dto.getAvailable());
            assertThat(json).extractingJsonPathNumberValue("$.requestId").isNull();
            assertThat(json).extractingJsonPathNumberValue("$.lastBooking.id").isNotNull();
            assertThat(json).extractingJsonPathNumberValue("$.nextBooking.id").isNotNull();
            assertThat(json).extractingJsonPathArrayValue("$.comments").isEmpty();
        }
    }

    @ParameterizedTest
    @MethodSource("getItemDto")
    void itemDtoDeserialize(ItemDto dto) throws IOException {
        String content = gson.toJson(dto);
        ItemDto result = testerDto.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(dto.getId());
        assertThat(result.getName()).isEqualTo(dto.getName());

        if (dto.getRequestId() != null) {
            assertThat(result.getDescription()).isEqualTo(dto.getDescription());
            assertThat(result.getAvailable()).isEqualTo(dto.getAvailable());
            assertThat(result.getRequestId()).isEqualTo(dto.getRequestId());
        }
        if (dto.getLastBooking() != null || dto.getNextBooking() != null) {
            assertThat(result.getDescription()).isEqualTo(dto.getDescription());
            assertThat(result.getAvailable()).isEqualTo(dto.getAvailable());
            assertThat(result.getRequestId()).isNull();
            assertThat(result.getLastBooking().getId()).isEqualTo(dto.getLastBooking().getId());
            assertThat(result.getNextBooking().getId()).isEqualTo(dto.getNextBooking().getId());
            assertThat(result.getComments()).isEmpty();
        }
    }

    private static Stream<Arguments> getItemDto() {
        return Stream.of(
                Arguments.of(ItemMapper.mapToItemDto(ITEM)),
                Arguments.of(ItemMapper.mapToItemDto(ITEM, REQUEST)),
                Arguments.of(ItemMapper.mapToItemDto(ITEM, Set.of())),
                Arguments.of(ItemMapper.mapToItemDto(ITEM, LAST_BOOKING, NEXT_BOOKING, Set.of()))
        );
    }
}
