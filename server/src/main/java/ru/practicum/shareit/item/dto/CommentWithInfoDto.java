package ru.practicum.shareit.item.dto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentWithInfoDto {
    private Long id;
    private String text;
    private String authorName;
    private LocalDateTime created;
}

