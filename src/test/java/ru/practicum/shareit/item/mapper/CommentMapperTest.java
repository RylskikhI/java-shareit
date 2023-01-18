package ru.practicum.shareit.item.mapper;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentWithInfoDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.dto.CommentDto;
import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {
    private static final User OWNER = new User(1L, "Nikolas", "nik@mail.ru");
    private static final User AUTHOR = new User(2L, "Bob", "bob@mail.ru");
    private static final Item ITEM = Item.builder()
            .id(1L)
            .name("Drill")
            .description("Drill 2000 MaxPro")
            .available(true)
            .owner(OWNER)
            .build();
    private static final Comment COMMENT = new Comment(1L, "Very good drill!", LocalDateTime.now(), ITEM, AUTHOR);
    private static final CommentDto DTO = CommentMapper.mapToCommentDto(COMMENT);

    @Test
    void toCommentInfoDto() {
        CommentWithInfoDto dto = CommentMapper.mapToCommentInfoDto(COMMENT);

        assertEquals(dto.getId(), COMMENT.getId());
        assertEquals(dto.getText(), COMMENT.getText());
        assertEquals(dto.getAuthorName(), COMMENT.getAuthor().getName());
        assertEquals(dto.getCreated(), COMMENT.getCreated());
    }

    @Test
    void toComment() {
        Comment comment = CommentMapper.mapToComment(DTO, ITEM, AUTHOR);

        assertEquals(comment.getId(), DTO.getId());
        assertEquals(comment.getText(), DTO.getText());
        assertEquals(comment.getItem(), ITEM);
        assertEquals(comment.getAuthor(), AUTHOR);
    }
}
