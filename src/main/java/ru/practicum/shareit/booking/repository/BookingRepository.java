package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByItemOwnerId(Long userId, Pageable pageable);

    List<Booking> findAllByItemOwnerId(Long userId, Sort sort);

    List<Booking> findAllByItemOwnerId(Long userId);

    List<Booking> findAllByBookerId(Long userId, Pageable pageable);

    List<Booking> findAllByItemId(Long itemId);
}
