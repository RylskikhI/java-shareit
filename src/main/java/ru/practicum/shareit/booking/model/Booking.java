package ru.practicum.shareit.booking.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Booking {
    private long bookingId;
    LocalDateTime start;
    LocalDateTime end;
    private long itemId;
    private long bookerId;
    private String status;
}
