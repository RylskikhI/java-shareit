package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemMapper {

    public static ItemDto mapToItemDto(@NotNull Item item, @NotNull Set<Comment> comments) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                null,
                null,
                comments.stream()
                        .map(CommentMapper::mapToCommentInfoDto)
                        .collect(Collectors.toSet())
        );
    }

    public static ItemDto mapToItemDto(@NotNull Item item,
                                       @NotNull Booking lastBooking,
                                       @NotNull Booking nextBooking,
                                       @NotNull Set<Comment> comments) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                BookingMapper.mapToBookingDtoWithIds(lastBooking),
                BookingMapper.mapToBookingDtoWithIds(nextBooking),
                comments.stream()
                        .map(CommentMapper::mapToCommentInfoDto)
                        .collect(Collectors.toSet())
        );
    }

    public static Item mapToItem(@NotNull ItemDto itemDto, @NotNull User owner) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner
        );
    }
}
