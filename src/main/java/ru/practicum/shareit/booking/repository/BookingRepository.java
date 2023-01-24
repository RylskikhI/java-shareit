package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByItemOwnerId(Long userId, Pageable pageable);

    List<Booking> findAllByItemOwnerId(Long userId, Sort sort);

    List<Booking> findAllByItemOwnerId(Long userId);

    List<Booking> findAllByBookerId(Long userId, Pageable pageable);

    List<Booking> findAllByItemId(Long itemId);

    List<Booking> findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfter(Long userId, LocalDateTime isBefore, LocalDateTime isAfter, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndEndIsBefore(Long userId, LocalDateTime date, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStartIsAfter(Long userId, LocalDateTime date, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStatusEquals(Long userId, BookingStatus status, Pageable pageable);

    List<Booking> findAllByBookerIdAndStartIsBeforeAndEndIsAfter(Long userId, LocalDateTime isBefore, LocalDateTime isAfter, Pageable pageable);

    List<Booking> findAllByBookerIdAndEndIsBefore(Long userId, LocalDateTime date, Pageable pageable);

    List<Booking> findAllByBookerIdAndStartIsAfter(Long userId, LocalDateTime date, Pageable pageable);

    List<Booking> findAllByBookerIdAndStatusEquals(Long userId, BookingStatus status, Pageable pageable);
}
