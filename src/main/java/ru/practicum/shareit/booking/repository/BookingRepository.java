package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByItemOwnerId(Long userId, Sort sort);

    List<Booking> findAllByBookerId(Long userId, Sort sort);

    List<Booking> findAllByItemId(Long itemId);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = ?1 AND b.item.id = ?2 " +
            "AND b.status <> ru.practicum.shareit.booking.model.BookingStatus.REJECTED ORDER BY b.id DESC")
    List<Booking> getBookingsByItemAndBooker(Long userId, Long itemId);

}
