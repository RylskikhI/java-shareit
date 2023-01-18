package ru.practicum.shareit.booking.service;

import java.util.Optional;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithEntities;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;

    @BeforeEach
    void init() {
        owner = new User(1L, "Nikolas", "nik@mail.ru");
        booker = new User(2L, "Djon", "djon@mail.ru");
        item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Drill 2000 MaxPro")
                .available(true)
                .owner(owner)
                .build();
        booking = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusDays(2), BookingStatus.WAITING, item, booker);
    }

    @AfterEach
    void tearDown() {
        owner = null;
        booker = null;
        item = null;
        booking = null;
    }

    @Test
    void findById() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        BookingDtoWithEntities dto = bookingService.findBookingById(owner.getId(), booking.getId());

        assertEquals(dto.getId(), booking.getId());
        assertEquals(dto.getStart(), booking.getStart());
        assertEquals(dto.getEnd(), booking.getEnd());
        assertEquals(dto.getStatus(), booking.getStatus());
        assertNotNull(dto.getItem().getId());
        assertNotNull(dto.getBooker());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findByNotValidUserId(Long userId) {
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            bookingService.findBookingById(userId, booking.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Пользователь с id=%d не найден!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findByNotValidId(Long id) {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        BookingNotFoundException exception = assertThrows(BookingNotFoundException.class, () -> {
            bookingService.findBookingById(owner.getId(), id);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Бронирование с id=%d не найдено!", id);

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findByOtherUserNotValidUserId(Long userId) {
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            bookingService.findBookingById(userId, booking.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Пользователь с id=%d не имеет прав на осуществление данного запроса!", userId);

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(userId);
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findAllByBookerNotValidUserId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.findAllByBookerId(userId, BookingState.ALL.name(), 0, 10);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Пользователь с id=%d не найден!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "", "PPS", "VENICE"})
    void findAllByBookerNotValidBookingState(String state) {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));

        BookingStateExistsException exception = assertThrows(BookingStateExistsException.class, () -> {
            bookingService.findAllByBookerId(booker.getId(), state, 0, 10);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = "Unknown state: UNSUPPORTED_STATUS";

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findAllByItemOwnerNotValidUserId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.findAllByItemOwnerId(userId, BookingState.ALL.name(), 0, 10);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Пользователь с id=%d не найден!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "", "PPS", "VENICE"})
    void findAllByItemOwnerNotValidBookingState(String state) {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        BookingStateExistsException exception = assertThrows(BookingStateExistsException.class, () -> {
            bookingService.findAllByItemOwnerId(owner.getId(), state, 0, 10);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = "Unknown state: UNSUPPORTED_STATUS";

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @Test
    void save() {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(booking);

        BookingDto dto = BookingMapper.mapToBookingDtoWithIds(booking);
        BookingDtoWithEntities savedDto = bookingService.createBooking(dto.getBookerId(), dto);

        assertEquals(savedDto.getId(), dto.getId());
        assertEquals(savedDto.getStart(), dto.getStart());
        assertEquals(savedDto.getEnd(), dto.getEnd());
        assertNotNull(savedDto.getItem());
        assertNotNull(savedDto.getBooker());

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void saveByOwner() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            BookingDto dto = BookingMapper.mapToBookingDtoWithIds(booking);
            bookingService.createBooking(owner.getId(), dto);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Пользователь с userId=%d является владельцем данной вещи!", owner.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
    }

    @Test
    void saveByNotValidAvailable() {
        item.setAvailable(false);
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        BookingStatusException exception = assertThrows(BookingStatusException.class, () -> {
            BookingDto dto = BookingMapper.mapToBookingDtoWithIds(booking);
            bookingService.createBooking(booker.getId(), dto);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Вещь available=%b, бронирование отклонено!", item.getAvailable());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
    }

    @Test
    void saveByNotValidUserId() {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            BookingDto dto = BookingMapper.mapToBookingDtoWithIds(booking);
            bookingService.createBooking(booker.getId(), dto);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Пользователь с id=%d не найден!", booker.getId());

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void saveByNotValidItemId() {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));

        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class, () -> {
            BookingDto dto = BookingMapper.mapToBookingDtoWithIds(booking);
            bookingService.createBooking(dto.getBookerId(), dto);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Вещь с id=%d не найдена!", item.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void update(Boolean approved) {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        BookingDtoWithEntities savedBooking = bookingService.updateBooking(owner.getId(), booking.getId(), approved);

        assertEquals(savedBooking.getId(), booking.getId());
        assertEquals(savedBooking.getStart(), booking.getStart());
        assertEquals(savedBooking.getEnd(), booking.getEnd());

        if (approved && booking.getStatus() == BookingStatus.WAITING) {
            assertEquals(savedBooking.getStatus(), BookingStatus.APPROVED);
        } else if (booking.getStatus() == BookingStatus.WAITING) {
            assertEquals(savedBooking.getStatus(), BookingStatus.REJECTED);
        }

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void updateByNotValidUserId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.updateBooking(userId, booking.getId(), false);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Пользователь с id=%d не найден!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void updateByBooker() {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            bookingService.updateBooking(booker.getId(), booking.getId(), false);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Пользователь с userId=%d не является владельцем данной вещи!", booker.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }

    @Test
    void updateByNotValidItemId() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.updateBooking(owner.getId(), booking.getId(), false);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Вещь с id=%d не найдена!", item.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }

    @Test
    void updateByNotValidId() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.updateBooking(owner.getId(), booking.getId(), false);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Бронирование с id=%d не найдено!", booking.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @Test
    void deleteById() {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        bookingService.deleteBookingById(booker.getId(), booking.getId());

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }

    @Test
    void deleteByNotValidUserId() {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.deleteBookingById(booker.getId(), booking.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Пользователь с id=%d не найден!", booker.getId());

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void deleteByNotValidId() {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.deleteBookingById(booker.getId(), booking.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Бронирование с id=%d не найдено!", booking.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void deleteByOtherUserNotValidUserId(Long userId) {
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.deleteBookingById(userId, booking.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Пользователь с id=%d не имеет права на удаление!", userId);

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(userId);
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }
}