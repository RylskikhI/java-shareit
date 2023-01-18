package ru.practicum.shareit.item.json;

import java.io.IOException;
import java.time.LocalDateTime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentWithInfoDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.utils.LocalDateTimeAdapter;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CommentJsonTest {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .serializeNulls()
            .create();
    private static final Item ITEM = Item.builder()
            .id(1L)
            .name("Drill")
            .description("Drill 2000 MaxPro")
            .available(true)
            .build();
    private static final User AUTHOR = new User(1L, "Nikolas", "nik@mail.ru");
    private static final Comment COMMENT = new Comment(1L, "Good drill!", LocalDateTime.now(), ITEM, AUTHOR);
    @Autowired
    private JacksonTester<CommentDto> testerDto;
    @Autowired
    private JacksonTester<CommentWithInfoDto> testerWithInfoDto;

    @Test
    void commentDtoSerialize() throws IOException {
        CommentDto dto = CommentMapper.mapToCommentDto(COMMENT);
        JsonContent<CommentDto> json = testerDto.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isNotNull();
        assertThat(json).extractingJsonPathStringValue("$.text").isEqualTo(dto.getText());
    }

    @Test
    void commentInfoDtoSerialize() throws IOException {
        CommentWithInfoDto dto = CommentMapper.mapToCommentInfoDto(COMMENT);
        JsonContent<CommentWithInfoDto> json = testerWithInfoDto.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isNotNull();
        assertThat(json).extractingJsonPathStringValue("$.text").isEqualTo(dto.getText());
        assertThat(json).extractingJsonPathStringValue("$.authorName").isEqualTo(dto.getAuthorName());
        assertThat(json).extractingJsonPathStringValue("$.created").isNotNull();
    }

    @Test
    void commentDtoDeserialize() throws IOException {
        CommentDto dto = CommentMapper.mapToCommentDto(COMMENT);
        String content = gson.toJson(dto);
        CommentDto result = testerDto.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(dto.getId());
        assertThat(result.getText()).isEqualTo(dto.getText());
    }

    @Test
    void commentDtoInfoDeserialize() throws IOException {
        CommentWithInfoDto dto = CommentMapper.mapToCommentInfoDto(COMMENT);
        String content = gson.toJson(dto);
        CommentWithInfoDto result = testerWithInfoDto.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(dto.getId());
        assertThat(result.getText()).isEqualTo(dto.getText());
        assertThat(result.getAuthorName()).isEqualTo(dto.getAuthorName());
        assertThat(result.getCreated()).isEqualTo(dto.getCreated());
    }
}
