package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Set;

@Setter
@Getter
@Builder
@ToString
public class ItemRequestDto {
    private Long id;
    @NotBlank
    private String description;
    @FutureOrPresent
    LocalDateTime created;
    private Set<ItemDto> items;
}
