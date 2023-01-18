package ru.practicum.shareit.booking.mapper;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDtoWithEntities;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {
    private static final User OWNER = new User(1L, "Nikolas", "nik@mail.ru");
    private static final User BOOKER = new User(2L, "Bob", "bob@mail.ru");
    private static final Item ITEM = Item.builder()
            .id(1L)
            .name("Drill")
            .description("Drill 2000 MaxPro")
            .available(true)
            .owner(OWNER)
            .build();
    private static final Booking BOOKING = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusDays(2),
            BookingStatus.WAITING, ITEM, BOOKER);
    private static final BookingDto DTO = BookingMapper.mapToBookingDtoWithIds(BOOKING);

    @Test
    void toBookingDto() {
        BookingDto dto = BookingMapper.mapToBookingDtoWithIds(BOOKING);

        assertEquals(dto.getId(), BOOKING.getId());
        assertEquals(dto.getStart(), BOOKING.getStart());
        assertEquals(dto.getEnd(), BOOKING.getEnd());
        assertEquals(dto.getItemId(), ITEM.getId());
        assertEquals(dto.getBookerId(), BOOKER.getId());
    }

    @Test
    void toBookingDtoInfo() {
        BookingDtoWithEntities dto = BookingMapper.mapToBookingDtoWithEntities(BOOKING);

        assertEquals(dto.getId(), BOOKING.getId());
        assertEquals(dto.getStart(), BOOKING.getStart());
        assertEquals(dto.getEnd(), BOOKING.getEnd());
        assertEquals(dto.getStatus(), BOOKING.getStatus());
        assertNotNull(dto.getItem());
        assertNotNull(dto.getBooker());
    }

    @Test
    void toBooking() {
        Booking booking = BookingMapper.mapToBooking(DTO, BookingStatus.WAITING, ITEM, BOOKER);

        assertEquals(booking.getId(), DTO.getId());
        assertEquals(booking.getStart(), DTO.getStart());
        assertEquals(booking.getEnd(), DTO.getEnd());
        assertEquals(booking.getItem().getId(), DTO.getItemId());
        assertEquals(booking.getBooker().getId(), DTO.getBookerId());
    }
}
