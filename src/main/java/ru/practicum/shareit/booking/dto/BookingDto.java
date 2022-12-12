package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */

@Data
@Builder
public class BookingDto {
    private long bookingId;
    LocalDateTime start;
    LocalDateTime end;
    private long itemId;
    private long bookerId;
    private String status;
}
