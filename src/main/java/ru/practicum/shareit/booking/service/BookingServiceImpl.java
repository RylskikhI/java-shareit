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
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.DESC;

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
        final MyPageRequest pageRequest = new MyPageRequest(from, size, Sort.by(DESC, "start"));
        return findAllByStateBooker(user.getId(), bookingState, pageRequest);
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
        final MyPageRequest pageRequest = new MyPageRequest(from, size, Sort.by(DESC, "start"));
        return findAllByStateOwner(user.getId(), bookingState, pageRequest);
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

    private List<BookingDtoWithEntities> findAllByStateOwner(Long userId, BookingState state, MyPageRequest pageRequest) {
        final LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        switch (state) {
            case CURRENT: {
                return bookingRepository.findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfter(userId, currentTime, currentTime, pageRequest).stream()
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(toList());
            }
            case PAST: {
                return bookingRepository.findAllByItemOwnerIdAndEndIsBefore(userId, currentTime, pageRequest).stream()
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(toList());
            }
            case FUTURE: {
                return bookingRepository.findAllByItemOwnerIdAndStartIsAfter(userId, currentTime, pageRequest).stream()
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(toList());
            }
            case WAITING: {
                return bookingRepository.findAllByItemOwnerIdAndStatusEquals(userId, BookingStatus.WAITING, pageRequest).stream()
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(toList());
            }
            case REJECTED: {
                return bookingRepository.findAllByItemOwnerIdAndStatusEquals(userId, BookingStatus.REJECTED, pageRequest).stream()
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(toList());
            }
            default: {
                return bookingRepository.findAllByItemOwnerId(userId, pageRequest).stream()
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(toList());
            }
        }
    }

    private List<BookingDtoWithEntities> findAllByStateBooker(Long userId, BookingState state, MyPageRequest pageRequest) {
        final LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        switch (state) {
            case CURRENT: {
                return bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfter(userId, currentTime, currentTime, pageRequest).stream()
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(toList());
            }
            case PAST: {
                return bookingRepository.findAllByBookerIdAndEndIsBefore(userId, currentTime, pageRequest).stream()
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(toList());
            }
            case FUTURE: {
                return bookingRepository.findAllByBookerIdAndStartIsAfter(userId, currentTime, pageRequest).stream()
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(toList());
            }
            case WAITING: {
                return bookingRepository.findAllByBookerIdAndStatusEquals(userId, BookingStatus.WAITING, pageRequest).stream()
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(toList());
            }
            case REJECTED: {
                return bookingRepository.findAllByBookerIdAndStatusEquals(userId, BookingStatus.REJECTED, pageRequest).stream()
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(toList());
            }
            default: {
                return bookingRepository.findAllByBookerId(userId, pageRequest).stream()
                        .map(BookingMapper::mapToBookingDtoWithEntities)
                        .collect(toList());
            }
        }
    }
}
