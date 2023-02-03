package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDtoWithEntities;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {
    private static final User OWNER = new User(1L, "John", "john@@gmail.com");
    private static final User BOOKER = new User(2L, "Fred", "fred@@gmail.com");
    private static final Item ITEM = Item.builder()
            .id(1L)
            .name("Saw")
            .description("Circular saw")
            .available(true)
            .owner(OWNER)
            .build();
    private static final Booking BOOKING = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusDays(2),
            BookingStatus.WAITING, ITEM, BOOKER);
    private static final BookingDto DTO = BookingMapper.mapToBookingDtoWithIds(BOOKING);

    @Test
    void mapToBookingDto() {
        BookingDto dto = BookingMapper.mapToBookingDtoWithIds(BOOKING);

        assertEquals(BOOKING.getId(), dto.getId());
        assertEquals(BOOKING.getStart(), dto.getStart());
        assertEquals(BOOKING.getEnd(), dto.getEnd());
        assertEquals(ITEM.getId(), dto.getItemId());
        assertEquals(BOOKER.getId(), dto.getBookerId());
    }

    @Test
    void mapToBookingDtoWithEntities() {
        BookingDtoWithEntities dto = BookingMapper.mapToBookingDtoWithEntities(BOOKING);

        assertEquals(BOOKING.getId(), dto.getId());
        assertEquals(BOOKING.getStart(), dto.getStart());
        assertEquals(BOOKING.getEnd(), dto.getEnd());
        assertEquals(BOOKING.getStatus(), dto.getStatus());
        assertNotNull(dto.getItem());
        assertNotNull(dto.getBooker());
    }

    @Test
    void mapToBooking() {
        Booking booking = BookingMapper.mapToBooking(DTO, BookingStatus.WAITING, ITEM, BOOKER);

        assertEquals(DTO.getId(), booking.getId());
        assertEquals(DTO.getStart(), booking.getStart());
        assertEquals(DTO.getEnd(), booking.getEnd());
        assertEquals(DTO.getItemId(), booking.getItem().getId());
        assertEquals(DTO.getBookerId(), booking.getBooker().getId());
    }
}
