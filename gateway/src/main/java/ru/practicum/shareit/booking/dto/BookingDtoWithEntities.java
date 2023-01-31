package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.validator.EndDateShouldBeAfterStartDate;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.BookerDto;

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
public class BookingDtoWithEntities {
    private Long id;
    @FutureOrPresent
    @NotNull
    private LocalDateTime start;
    @Future
    @NotNull
    private LocalDateTime end;
    @NotNull
    private BookingStatus status;
    @NotNull
    private ItemDto item;
    @NotNull
    private BookerDto booker;
}
