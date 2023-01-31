package ru.practicum.shareit.booking;

import java.util.Set;
import java.util.List;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.TypedQuery;
import javax.persistence.EntityManager;

import ru.practicum.shareit.booking.dto.BookingDtoWithEntities;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingDbTest {
    private User owner;
    private User booker;
    private Booking booking;
    private Item item;
    private final EntityManager em;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;

    @BeforeEach
    void init() {
        owner = new User(null, "Jack", "jack@gmail.com");
        booker = new User(null, "Hugo", "hugo@yahoo.com");
        item = Item.builder()
                .name("Screwdriver")
                .description("Torx screwdriver")
                .available(true)
                .owner(owner)
                .build();
        booking = new Booking(null, LocalDateTime.now(), LocalDateTime.now().plusDays(5), BookingStatus.WAITING, item, booker);
    }

    @AfterEach
    void tearDown() {
        owner = null;
        booker = null;
        item = null;
        booking = null;
    }

    @Test
    void findBookingById() {
        BookingDtoWithEntities dto = addBooking(booking);
        TypedQuery<Long> query = em.createQuery("select b.id from Booking as b where b.id = :id", Long.class);
        Long id = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        dto = bookingService.findBookingById(owner.getId(), id);

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getStart()).isBefore(LocalDateTime.now());
        assertThat(dto.getEnd()).isAfter(LocalDateTime.now());
        assertThat(dto.getStatus()).isEqualTo(booking.getStatus());
        assertThat(dto.getItem()).isNotNull();
        assertThat(dto.getBooker()).isNotNull();
    }

    @Test
    void findAllByBookerId() {
        BookingDtoWithEntities dto = addBooking(booking);
        TypedQuery<Booking> query = em.createQuery("select b from Booking as b where b.booker.id = :id", Booking.class);
        List<Booking> result = query
                .setParameter("id", booker.getId())
                .getResultList();

        List<BookingDtoWithEntities> bookings = bookingService.findAllByBookerId(booker.getId(), BookingState.ALL.name(), 0, 10);

        assertThat(result.size()).isEqualTo(bookings.size());
    }

    @Test
    void findAllByItemOwnerId() {
        BookingDtoWithEntities dto = addBooking(booking);
        TypedQuery<Booking> query = em.createQuery("select b from Booking as b where b.item.owner.id = :id",
                Booking.class);
        List<Booking> result = query
                .setParameter("id", owner.getId())
                .getResultList();

        List<BookingDtoWithEntities> bookings = bookingService.findAllByItemOwnerId(owner.getId(), BookingState.ALL.name(), 0, 10);

        assertThat(result.size()).isEqualTo(bookings.size());
    }

    @Test
    void createBooking() {
        BookingDtoWithEntities dto = addBooking(booking);
        TypedQuery<Booking> query = em.createQuery("select b from Booking as b where b.booker.email = :email", Booking.class);
        booking = query
                .setParameter("email", booker.getEmail())
                .getSingleResult();

        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getStart()).isBefore(LocalDateTime.now());
        assertThat(booking.getEnd()).isAfter(LocalDateTime.now());
        assertThat(booking.getStatus()).isEqualTo(dto.getStatus());
        assertThat(booking.getItem()).isNotNull();
        assertThat(booking.getBooker()).isNotNull();
    }

    @Test
    void deleteBookingById() {
        BookingDtoWithEntities dto = addBooking(booking);
        TypedQuery<Long> query = em.createQuery("select b.id from Booking as b where b.id = :id", Long.class);
        Long id = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        bookingService.deleteBookingById(owner.getId(), id);
        BookingNotFoundException exception = assertThrows(BookingNotFoundException.class, () -> {
            bookingService.findBookingById(owner.getId(), id);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Бронирование с id=%d не найдено!", id);

        assertEquals(expectedMessage, actualMessage);
    }

    private BookingDtoWithEntities addBooking(Booking booking) {
        UserDto userDto = UserMapper.mapToUserDto(owner);
        userDto = userService.saveUser(userDto);
        owner.setId(userDto.getId());

        userDto = UserMapper.mapToUserDto(booker);
        userDto = userService.saveUser(userDto);
        booker.setId(userDto.getId());

        ItemDto itemDto = ItemMapper.mapToItemDto(item, Set.of());
        itemDto = itemService.addNewItem(owner.getId(), itemDto);
        item.setId(itemDto.getId());

        BookingDto bookingDto = BookingMapper.mapToBookingDtoWithIds(booking);
        return bookingService.createBooking(booker.getId(), bookingDto);
    }
}
