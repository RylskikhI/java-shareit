package ru.practicum.shareit.item.dto;

import lombok.*;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentWithInfoDto {
    private Long id;
    @NotBlank
    private String text;
    @NotBlank
    private String authorName;
    @FutureOrPresent
    private LocalDateTime created;
}
