package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoWithEntities;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.MyPageRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository,
                              ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    @Override
    public BookingDtoWithEntities createBooking(Long userId, BookingDto bookingDto) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id=%d не найден!", userId))
        );
        final Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(
                () -> new ItemNotFoundException(String.format("Вещь с id=%d не найдена!", bookingDto.getItemId()))
        );
        if (item.getOwner().getId().equals(user.getId())) {
            throw new UserNotFoundException(String.format("Пользователь с userId=%d является владельцем данной вещи!", userId));
        }
        if (!item.getAvailable()) {
            throw new BookingStatusException(String.format("Вещь available=%b, бронирование отклонено!", item.getAvailable()));
        }
        final Booking booking = BookingMapper.mapToBooking(bookingDto, BookingStatus.WAITING, item, user);
        final Booking bookingToSave = bookingRepository.save(booking);
        return BookingMapper.mapToBookingDtoWithEntities(bookingToSave);
    }

    @Override
    public BookingDtoWithEntities findBookingById(Long userId, Long id) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("Пользователь с id=%d не найден!", userId))
        );
        final Booking booking = bookingRepository.findById(id).orElseThrow(
                () -> new BookingNotFoundException(String.format("Бронирование с id=%d не найдено!", id))
        );
        final User booker = booking.getBooker();
        final User owner = booking.getItem().getOwner();

        if (booker.getId().equals(user.getId()) || owner.getId().equals(user.getId())) {
            return BookingMapper.mapToBookingDtoWithEntities(booking);
        }
        throw new UserNotFoundException(String.format("Пользователь с id=%d не имеет прав на осуществление данного запроса!", userId));
    }

    @Override
    public List<BookingDtoWithEntities> findAllByBookerId(Long userId, String state, Integer from, Integer size) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id=%d не найден!", userId))
        );
        final BookingState bookingState = BookingState.from(state);
        if (bookingState == null) {
            throw new BookingStateExistsException("Unknown state: UNSUPPORTED_STATUS");
        }
        MyPageRequest pageRequest = new MyPageRequest(from, size, Sort.by(Sort.Direction.DESC, "start"));
        List<Booking> bookings = bookingRepository.findAllByBookerId(user.getId(), pageRequest);
        return findAllByState(bookingState, bookings);
    }

    @Override
    public List<BookingDtoWithEntities> findAllByItemOwnerId(Long userId, String state, Integer from, Integer size) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id=%d не найден!", userId))
        );
        final BookingState bookingState = BookingState.from(state);
        if (bookingState == null) {
            throw new BookingStateExistsException("Unknown state: UNSUPPORTED_STATUS");
        }
        MyPageRequest pageRequest = new MyPageRequest(from, size, Sort.by(Sort.Direction.DESC, "start"));
        List<Booking> bookings = bookingRepository.findAllByItemOwnerId(user.getId(), pageRequest);
        return findAllByState(bookingState, bookings);
    }

    @Override
    @Transactional
    public BookingDtoWithEntities updateBooking(Long userId, Long id, Boolean approved) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id=%d не найден!", userId))
        );
        final Booking booking = bookingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Бронирование с id=%d не найдено!", id))
        );
        final Item item = itemRepository.findById(booking.getItem().getId()).orElseThrow(
                () -> new EntityNotFoundException(String.format("Вещь с id=%d не найдена!", booking.getItem().getId()))
        );
        if (!item.getOwner().getId().equals(user.getId())) {
            throw new UserNotFoundException(String.format("Пользователь с userId=%d не является владельцем данной вещи!", userId));
        }
        if (approved && booking.getStatus() == BookingStatus.WAITING) {
            booking.setStatus(BookingStatus.APPROVED);
        } else if (booking.getStatus() == BookingStatus.WAITING) {
            booking.setStatus(BookingStatus.REJECTED);
        } else {
            throw new BookingStatusException(String.format("Статус бронирования=%s!", booking.getStatus()));
        }

        return BookingMapper.mapToBookingDtoWithEntities(booking);
    }

    @Override
    @Transactional
    public void deleteBookingById(Long userId, Long id) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id=%d не найден!", userId))
        );
        final Booking booking = bookingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Бронирование с id=%d не найдено!", id))
        );
        final User booker = booking.getBooker();
        final Long owner = booking.getItem().getOwner().getId();
        if (booker.getId().equals(user.getId()) || owner.equals(user.getId())) {
            bookingRepository.deleteById(booking.getId());
        } else {
            throw new EntityNotFoundException(String.format("Пользователь с id=%d не имеет права на удаление!", userId));
        }
    }

    public List<BookingDtoWithEntities> findAllByState(BookingState state, List<Booking> bookings) {
        final LocalDateTime currentTime = LocalDateTime.now();

        switch (state) {
            case CURRENT: {
                return bookings.stream()
                        .filter(it -> it.getStart().isBefore(currentTime) && it.getEnd().isAfter(currentTime))
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(Collectors.toList());
            }
            case PAST: {
                return bookings.stream()
                        .filter(it -> it.getEnd().isBefore(currentTime))
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(Collectors.toList());
            }
            case FUTURE: {
                return bookings.stream()
                        .filter(it -> it.getStart().isAfter(currentTime))
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(Collectors.toList());
            }
            case WAITING: {
                return bookings.stream()
                        .filter(it -> it.getStatus() == BookingStatus.WAITING)
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(Collectors.toList());
            }
            case REJECTED: {
                return bookings.stream()
                        .filter(it -> it.getStatus() == BookingStatus.REJECTED)
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(Collectors.toList());
            }
            default: {
                return bookings.stream()
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(Collectors.toList());
            }
        }
    }
}
