package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.validator.EndDateShouldBeAfterStartDate;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EndDateShouldBeAfterStartDate
public class BookingDto {
    private Long id;
    @FutureOrPresent
    @NotNull
    private LocalDateTime start;
    @Future
    @NotNull
    private LocalDateTime end;
    private BookingStatus status;
    @NotNull
    private Long itemId;
    private Long bookerId;
}
