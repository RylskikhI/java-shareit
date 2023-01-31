package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class ItemMapper {

    public static ItemDto mapToItemDto(@NotNull Item item, @NotNull Set<Comment> comments) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(comments == null ? Collections.emptySet() : comments.stream()
                        .map(CommentMapper::mapToCommentInfoDto)
                        .collect(toSet()))
                .build();
    }

    public static ItemDto mapToItemDto(@NotNull Item item,
                                       @NotNull Booking lastBooking,
                                       @NotNull Booking nextBooking,
                                       @NotNull Set<Comment> comments) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(BookingMapper.mapToBookingDtoWithIds(lastBooking))
                .nextBooking(BookingMapper.mapToBookingDtoWithIds(nextBooking))
                .comments(comments == null ? Collections.emptySet() : comments.stream()
                        .map(CommentMapper::mapToCommentInfoDto)
                        .collect(toSet()))
                .build();
    }

    public static ItemDto mapToItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .build();
    }

    public static Item mapToItem(@NotNull ItemDto itemDto, @NotNull User owner) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .build();
    }

    public static ItemDto mapToItemDto(Item item, ItemRequest request) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(request.getId())
                .build();
    }

    public static Item mapToItem(ItemDto itemDto, User owner, ItemRequest request) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .request(request)
                .build();
    }
}
