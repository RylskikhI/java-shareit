package ru.practicum.shareit.request;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
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
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.utils.LocalDateTimeAdapter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestJsonTest {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .serializeNulls()
            .create();
    private static final User REQUESTOR = new User(2L, "George", "george@gmail.com");
    private static final ItemRequest REQUEST = new ItemRequest(1L, "Torque screwdriver", LocalDateTime.now().withNano(0),
            REQUESTOR);
    @Autowired
    private JacksonTester<ItemRequestDto> testerDto;

    @ParameterizedTest
    @MethodSource("getItemRequestDto")
    void itemRequestDtoSerialize(ItemRequestDto dto) throws IOException {
        JsonContent<ItemRequestDto> json = testerDto.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isNotNull();
        assertThat(json).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(json).extractingJsonPathStringValue("$.created").isEqualTo(dto.getCreated().toString());

        if (dto.getItems() != null) {
            assertThat(json).extractingJsonPathArrayValue("$.items").isEmpty();
        }
    }

    @ParameterizedTest
    @MethodSource("getItemRequestDto")
    void itemRequestDtoDeserialize(ItemRequestDto dto) throws IOException {
        String content = gson.toJson(dto);
        ItemRequestDto result = testerDto.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(dto.getId());
        assertThat(result.getDescription()).isEqualTo(dto.getDescription());
        assertThat(result.getCreated()).isEqualTo(dto.getCreated());

        if (dto.getItems() != null) {
            assertThat(result.getItems()).isEmpty();
        }
    }

    private static Stream<Arguments> getItemRequestDto() {
        return Stream.of(
                Arguments.of(ItemRequestMapper.mapToItemRequestDto(REQUEST)),
                Arguments.of(ItemRequestMapper.mapToItemRequestDto(REQUEST, Set.of()))
        );
    }
}
