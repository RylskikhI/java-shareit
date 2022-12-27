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
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final Item itemWrap = itemRepository.findById(bookingDto.getItemId()).orElseThrow(
                () -> new ItemNotFoundException(String.format("Item with id=%d not found!", bookingDto.getItemId()))
        );
        if (itemWrap.getOwner().getId().equals(userWrap.getId())) {
            throw new UserNotFoundException(String.format("User userId=%d is the owner of the item!", userId));
        }
        if (!itemWrap.getAvailable()) {
            throw new BookingStatusException(String.format("Item available=%b, booking rejected!", itemWrap.getAvailable()));
        }
        final Booking booking = BookingMapper.mapToBooking(bookingDto, BookingStatus.WAITING, itemWrap, userWrap);
        final Booking bookingWrap = bookingRepository.save(booking);
        return BookingMapper.mapToBookingDtoWithEntities(bookingWrap);
    }

    @Override
    public BookingDtoWithEntities findById(Long userId, Long id) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final Booking bookingWrap = bookingRepository.findById(id).orElseThrow(
                () -> new BookingNotFoundException(String.format("Booking with id=%d not found!", id))
        );
        final User booker = bookingWrap.getBooker();
        final User owner = bookingWrap.getItem().getOwner();

        if (booker.getId().equals(userWrap.getId()) || owner.getId().equals(userWrap.getId())) {
            return BookingMapper.mapToBookingDtoWithEntities(bookingWrap);
        }
        throw new UserNotFoundException(String.format("User with id=%d does not have the right to request extraction!", userId));
    }

    @Override
    public List<BookingDtoWithEntities> findAllByBookerId(Long userId, String state) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final BookingState bookingState = BookingState.from(state);
        if (bookingState == null) {
            throw new BookingStateExistsException("Unknown state: UNSUPPORTED_STATUS");
        }
        List<Booking> bookings = bookingRepository.findAllByBookerId(userWrap.getId(), Sort.by(Sort.Direction.DESC, "start"));
        return findAllByState(bookingState, bookings);
    }

    @Override
    public List<BookingDtoWithEntities> findAllByItemOwnerId(Long userId, String state) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final BookingState bookingState = BookingState.from(state);
        if (bookingState == null) {
            throw new BookingStateExistsException("Unknown state: UNSUPPORTED_STATUS");
        }
        List<Booking> bookings = bookingRepository.findAllByItemOwnerId(userWrap.getId(), Sort.by(Sort.Direction.DESC, "start"));
        return findAllByState(bookingState, bookings);
    }

    @Override
    @Transactional
    public BookingDtoWithEntities update(Long userId, Long id, String approved) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final Booking bookingWrap = bookingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Booking with id=%d not found!", id))
        );
        final Item itemWrap = itemRepository.findById(bookingWrap.getItem().getId()).orElseThrow(
                () -> new EntityNotFoundException(String.format("Item with id=%d not found!", bookingWrap.getItem().getId()))
        );
        if (!itemWrap.getOwner().getId().equals(userWrap.getId())) {
            throw new UserNotFoundException(String.format("User userId=%d is not the owner of the item!", userId));
        }
        if (Boolean.parseBoolean(approved) && bookingWrap.getStatus() == BookingStatus.WAITING) {
            bookingWrap.setStatus(BookingStatus.APPROVED);
        } else if (bookingWrap.getStatus() == BookingStatus.WAITING) {
            bookingWrap.setStatus(BookingStatus.REJECTED);
        } else {
            throw new BookingStatusException(String.format("Booking status=%s!", bookingWrap.getStatus()));
        }
        bookingRepository.save(bookingWrap);
        return BookingMapper.mapToBookingDtoWithEntities(bookingWrap);
    }

    @Override
    @Transactional
    public void deleteById(Long userId, Long id) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final Booking bookingWrap = bookingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Booking with id=%d not found!", id))
        );
        final User booker = bookingWrap.getBooker();
        final Long owner = bookingWrap.getItem().getOwner().getId();
        if (booker.getId().equals(userWrap.getId()) || owner.equals(userWrap.getId())) {
            bookingRepository.deleteById(bookingWrap.getId());
        } else {
            throw new EntityNotFoundException(String.format("User with id=%d does not have the right to request deletion!", userId));
        }
    }

    private List<BookingDtoWithEntities> findAllByState(BookingState state, List<Booking> bookings) {
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
