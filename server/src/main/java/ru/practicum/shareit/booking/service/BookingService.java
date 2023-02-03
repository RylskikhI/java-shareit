package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithEntities;

import java.util.List;

public interface BookingService {

    /**
     * Создание бронирования.
     * @param bookingDto Entity.
     * @param userId User id.
     * @return BookingDtoWithEntities.
     */
    BookingDtoWithEntities createBooking(Long userId, BookingDto bookingDto);

    /**
     * Поиск бронирования по id.
     * @param userId User id.
     * @param id Booking id.
     * @return BookingDtoWithEntities.
     */
    BookingDtoWithEntities findBookingById(Long userId, Long id);

    /**
     * Поиск всех бронирований по бронирующему, сортировка по убыванию даты старта.
     * @param userId User id.
     * @param state Booking state.
     * @return List BookingDtoWithEntities.
     */
    List<BookingDtoWithEntities> findAllByBookerId(Long userId, String state, Integer from, Integer size);

    /**
     * Поиск всех бронирований по владельцу, сортировка по убыванию даты старта.
     * @param userId User id.
     * @param state Booking state.
     * @return List BookingDtoWithEntities.
     */
    List<BookingDtoWithEntities> findAllByItemOwnerId(Long userId, String state, Integer from, Integer size);

    /**
     * Обновить бронирование по id. Подтверждение или отклонение запроса на бронирование.
     * @param userId User id.
     * @param id Booking id.
     * @param approved Параметр принимает true или false.
     * @return BookingDtoWithEntities.
     */
    BookingDtoWithEntities updateBooking(Long userId, Long id, Boolean approved);

    /**
     * Удалить бронирование по id.
     * @param userId User id.
     * @param id Booking id.
     */
    void deleteBookingById(Long userId, Long id);
}
