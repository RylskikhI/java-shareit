package ru.practicum.shareit.request.dto;

import lombok.*;
import java.util.Set;
import java.time.LocalDateTime;
import javax.validation.constraints.*;
import ru.practicum.shareit.item.dto.ItemDto;

@Setter
@Getter
@Builder
@ToString
public class ItemRequestDto {
    private Long id;
    @NotBlank
    private String description;
    @FutureOrPresent
    private LocalDateTime created;
    private Set<ItemDto> items;
}
