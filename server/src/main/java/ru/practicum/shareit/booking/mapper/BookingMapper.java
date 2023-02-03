package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithEntities;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotNull;

public class BookingMapper {

    public static BookingDto mapToBookingDtoWithIds(@NotNull Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                booking.getItem().getId(),
                booking.getBooker().getId()
        );
    }

    public static Booking mapToBooking(@NotNull BookingDto bookingDto,
                                       @NotNull BookingStatus status,
                                       @NotNull Item item,
                                       @NotNull User booker) {
        return new Booking(
                bookingDto.getId(),
                bookingDto.getStart(),
                bookingDto.getEnd(),
                status,
                item,
                booker
        );
    }

    public static BookingDtoWithEntities mapToBookingDtoWithEntities(@NotNull Booking booking) {
        return new BookingDtoWithEntities(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                booking.getItem(),
                UserMapper.toBookerDto(booking.getBooker())
        );
    }
}
