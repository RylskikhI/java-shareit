package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithEntities;

import java.util.List;

public interface BookingService {

    BookingDtoWithEntities createBooking(Long userId, BookingDto bookingDto);

    BookingDtoWithEntities findById(Long userId, Long id);

    List<BookingDtoWithEntities> findAllByBookerId(Long userId, String state);

    List<BookingDtoWithEntities> findAllByItemOwnerId(Long userId, String state);

    BookingDtoWithEntities update(Long userId, Long id, String approved);

    void deleteById(Long userId, Long id);
}
