package ru.practicum.shareit.booking;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithEntities;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.MyPageRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;

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
        owner = new User(1L, "John", "john@gmail.com");
        booker = new User(2L, "Fred", "fred@gmail.com");
        item = Item.builder()
                .id(1L)
                .name("Saw")
                .description("Circular saw")
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
    void findBookingById() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        BookingDtoWithEntities dto = bookingService.findBookingById(owner.getId(), booking.getId());

        assertEquals(booking.getId(), dto.getId());
        assertEquals(booking.getStart(), dto.getStart());
        assertEquals(booking.getEnd(), dto.getEnd());
        assertEquals(booking.getStatus(), dto.getStatus());
        assertNotNull(dto.getItem().getId());
        assertNotNull(dto.getBooker());

        Mockito.verify(userRepository, times(1)).findById(owner.getId());
        Mockito.verify(bookingRepository, times(1)).findById(booking.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findBookingByNotValidUserId(Long userId) {
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            bookingService.findBookingById(userId, booking.getId());
        });

        String expectedMessage = String.format("Пользователь с id=%d не найден!", userId);
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findBookingByNotValidId(Long id) {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        BookingNotFoundException exception = assertThrows(BookingNotFoundException.class, () -> {
            bookingService.findBookingById(owner.getId(), id);
        });

        String expectedMessage = String.format("Бронирование с id=%d не найдено!", id);
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, times(1)).findById(owner.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findBookingByOtherUserNotValidUserId(Long userId) {
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            bookingService.findBookingById(userId, booking.getId());
        });

        String expectedMessage = String.format("Пользователь с id=%d не имеет прав на осуществление данного запроса!", userId);
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, times(1)).findById(userId);
        Mockito.verify(bookingRepository, times(1)).findById(booking.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findAllBookingsByBookerNotValidUserId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.findAllByBookerId(userId, BookingState.ALL.name(), 0, 10);
        });

        String expectedMessage = String.format("Пользователь с id=%d не найден!", userId);
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "", "PPS", "VENICE"})
    void findAllBookingsByBookerNotValidBookingState(String state) {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));

        BookingStateExistsException exception = assertThrows(BookingStateExistsException.class, () -> {
            bookingService.findAllByBookerId(booker.getId(), state, 0, 10);
        });

        String expectedMessage = "Unknown state: UNSUPPORTED_STATUS";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, times(1)).findById(booker.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findAllBookingsByItemOwnerNotValidUserId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.findAllByItemOwnerId(userId, BookingState.ALL.name(), 0, 10);
        });

        String expectedMessage = String.format("Пользователь с id=%d не найден!", userId);
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void createBooking() {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(booking);

        BookingDto dto = BookingMapper.mapToBookingDtoWithIds(booking);
        BookingDtoWithEntities savedDto = bookingService.createBooking(dto.getBookerId(), dto);

        assertEquals(dto.getId(), savedDto.getId());
        assertEquals(dto.getStart(), savedDto.getStart());
        assertEquals(dto.getEnd(), savedDto.getEnd());
        assertNotNull(savedDto.getItem());
        assertNotNull(savedDto.getBooker());

        Mockito.verify(userRepository, times(1)).findById(booker.getId());
        Mockito.verify(itemRepository, times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, times(1)).save(Mockito.any());
    }

    @Test
    void createBookingByOwner() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            BookingDto dto = BookingMapper.mapToBookingDtoWithIds(booking);
            bookingService.createBooking(owner.getId(), dto);
        });

        String expectedMessage = String.format("Пользователь с userId=%d является владельцем данной вещи!", owner.getId());
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, times(1)).findById(item.getId());
    }

    @Test
    void createBookingByNotValidAvailable() {
        item.setAvailable(false);
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        BookingStatusException exception = assertThrows(BookingStatusException.class, () -> {
            BookingDto dto = BookingMapper.mapToBookingDtoWithIds(booking);
            bookingService.createBooking(booker.getId(), dto);
        });

        String expectedMessage = String.format("Вещь available=%b, бронирование отклонено!", item.getAvailable());
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, times(1)).findById(booker.getId());
        Mockito.verify(itemRepository, times(1)).findById(item.getId());
    }

    @Test
    void createBookingByNotValidUserId() {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            BookingDto dto = BookingMapper.mapToBookingDtoWithIds(booking);
            bookingService.createBooking(booker.getId(), dto);
        });

        String expectedMessage = String.format("Пользователь с id=%d не найден!", booker.getId());
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void createBookingByNotValidItemId() {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));

        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class, () -> {
            BookingDto dto = BookingMapper.mapToBookingDtoWithIds(booking);
            bookingService.createBooking(dto.getBookerId(), dto);
        });

        String expectedMessage = String.format("Вещь с id=%d не найдена!", item.getId());
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, times(1)).findById(booker.getId());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void updateBooking(Boolean approved) {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        BookingDtoWithEntities savedBooking = bookingService.updateBooking(owner.getId(), booking.getId(), approved);

        assertEquals(booking.getId(), savedBooking.getId());
        assertEquals(booking.getStart(), savedBooking.getStart());
        assertEquals(booking.getEnd(), savedBooking.getEnd());

        if (approved && booking.getStatus() == BookingStatus.WAITING) {
            assertEquals(BookingStatus.APPROVED, savedBooking.getStatus());
        } else if (booking.getStatus() == BookingStatus.WAITING) {
            assertEquals(BookingStatus.REJECTED, savedBooking.getStatus());
        }

        Mockito.verify(userRepository, times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, times(1)).findById(booking.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void updateBookingByNotValidUserId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.updateBooking(userId, booking.getId(), false);
        });

        String expectedMessage = String.format("Пользователь с id=%d не найден!", userId);
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void updateBookingByBooker() {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            bookingService.updateBooking(booker.getId(), booking.getId(), false);
        });

        String expectedMessage = String.format("Пользователь с userId=%d не является владельцем данной вещи!", booker.getId());
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, times(1)).findById(booker.getId());
        Mockito.verify(itemRepository, times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, times(1)).findById(booking.getId());
    }

    @Test
    void updateBookingByNotValidItemId() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.updateBooking(owner.getId(), booking.getId(), false);
        });

        String expectedMessage = String.format("Вещь с id=%d не найдена!", item.getId());
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, times(1)).findById(owner.getId());
        Mockito.verify(bookingRepository, times(1)).findById(booking.getId());
    }

    @Test
    void updateBookingByNotValidId() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.updateBooking(owner.getId(), booking.getId(), false);
        });

        String expectedMessage = String.format("Бронирование с id=%d не найдено!", booking.getId());
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, times(1)).findById(owner.getId());
    }

    @Test
    void deleteBookingById() {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        bookingService.deleteBookingById(booker.getId(), booking.getId());

        Mockito.verify(userRepository, times(1)).findById(booker.getId());
        Mockito.verify(bookingRepository, times(1)).findById(booking.getId());
    }

    @Test
    void deleteBookingByNotValidUserId() {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.deleteBookingById(booker.getId(), booking.getId());
        });

        String expectedMessage = String.format("Пользователь с id=%d не найден!", booker.getId());
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void deleteBookingByNotValidId() {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.deleteBookingById(booker.getId(), booking.getId());
        });

        String expectedMessage = String.format("Бронирование с id=%d не найдено!", booking.getId());
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, times(1)).findById(booker.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void deleteBookingByOtherUserNotValidUserId(Long userId) {
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.deleteBookingById(userId, booking.getId());
        });

        String expectedMessage = String.format("Пользователь с id=%d не имеет права на удаление!", userId);
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, times(1)).findById(userId);
        Mockito.verify(bookingRepository, times(1)).findById(booking.getId());
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void findAllByBookerId(BookingState state) {
        MyPageRequest pageRequest = new MyPageRequest(0, 10, Sort.by(Sort.Direction.DESC, "start"));
        LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        switch (state) {
            case CURRENT: {
                Mockito.when(bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfter(booker.getId(), currentTime,
                        currentTime, pageRequest)).thenReturn(List.of(booking));
                break;
            }
            case PAST: {
                booking.setEnd(LocalDateTime.now().minusDays(5));
                Mockito.when(bookingRepository.findAllByBookerIdAndEndIsBefore(booker.getId(), currentTime,
                        pageRequest)).thenReturn(List.of(booking));
                break;
            }
            case FUTURE: {
                booking.setStart(LocalDateTime.now().plusDays(5));
                Mockito.when(bookingRepository.findAllByBookerIdAndStartIsAfter(booker.getId(), currentTime,
                        pageRequest)).thenReturn(List.of(booking));
                break;
            }
            case WAITING: {
                Mockito.when(bookingRepository.findAllByBookerIdAndStatusEquals(booker.getId(), booking.getStatus(),
                        pageRequest)).thenReturn(List.of(booking));
                break;
            }
            case REJECTED: {
                booking.setStatus(BookingStatus.REJECTED);
                Mockito.when(bookingRepository.findAllByBookerIdAndStatusEquals(booker.getId(), booking.getStatus(),
                        pageRequest)).thenReturn(List.of(booking));
                break;
            }
            case ALL: {
                Mockito.when(bookingRepository.findAllByBookerId(booker.getId(), pageRequest)).thenReturn(List.of(booking));
                break;
            }
        }

        List<BookingDtoWithEntities> bookings = bookingService.findAllByBookerId(booker.getId(), state.name(), 0, 10);

        assertEquals(1, bookings.size());
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void findAllByItemOwnerId(BookingState state) {
        MyPageRequest pageRequest = new MyPageRequest(0, 10, Sort.by(Sort.Direction.DESC, "start"));
        LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        switch (state) {
            case CURRENT: {
                Mockito.when(bookingRepository.findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfter(owner.getId(), currentTime,
                        currentTime, pageRequest)).thenReturn(List.of(booking));
                break;
            }
            case PAST: {
                booking.setEnd(currentTime.minusDays(5));
                Mockito.when(bookingRepository.findAllByItemOwnerIdAndEndIsBefore(owner.getId(), currentTime,
                        pageRequest)).thenReturn(List.of(booking));
                break;
            }
            case FUTURE: {
                booking.setStart(currentTime.plusDays(5));
                Mockito.when(bookingRepository.findAllByItemOwnerIdAndStartIsAfter(owner.getId(), currentTime,
                        pageRequest)).thenReturn(List.of(booking));
                break;
            }
            case WAITING: {
                Mockito.when(bookingRepository.findAllByItemOwnerIdAndStatusEquals(owner.getId(), booking.getStatus(),
                        pageRequest)).thenReturn(List.of(booking));
                break;
            }
            case REJECTED: {
                booking.setStatus(BookingStatus.REJECTED);
                Mockito.when(bookingRepository.findAllByItemOwnerIdAndStatusEquals(owner.getId(), booking.getStatus(),
                        pageRequest)).thenReturn(List.of(booking));
                break;
            }
            case ALL: {
                Mockito.when(bookingRepository.findAllByItemOwnerId(owner.getId(), pageRequest)).thenReturn(List.of(booking));
                break;
            }
        }

        List<BookingDtoWithEntities> bookings = bookingService.findAllByItemOwnerId(owner.getId(), state.name(), 0, 10);

        assertEquals(1, bookings.size());
    }
}
