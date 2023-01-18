package ru.practicum.shareit.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {
    public static ItemRequestDto mapToItemRequestDto(ItemRequest request) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .build();
    }

    public static ItemRequestDto mapToItemRequestDto(ItemRequest request, Set<Item> items) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items == null ? Collections.emptySet() : items.stream()
                        .map(it -> ItemMapper.mapToItemDto(it, request))
                        .collect(toSet()))
                .build();
    }

    public static ItemRequest mapToItemRequest(ItemRequestDto requestDto, User requestor) {
        return new ItemRequest(
                requestDto.getId(),
                requestDto.getDescription(),
                LocalDateTime.now(),
                requestor
        );
    }
}
