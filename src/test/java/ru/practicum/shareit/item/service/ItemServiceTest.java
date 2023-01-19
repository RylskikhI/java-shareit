package ru.practicum.shareit.item.service;

import org.mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.SecurityException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.CommentWithInfoDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    private User owner;
    private User requestor;
    private ItemRequest request;
    private Item item;
    private Comment comment;
    private static final User BOOKER = new User(3L, "Jack", "jack@gmail.com");
    private static final Booking LAST_BOOKING = new Booking(1L, LocalDateTime.now().minusDays(5),
            LocalDateTime.now().minusDays(2), BookingStatus.WAITING, Item.builder().id(1L).build(), BOOKER);
    private static final Booking NEXT_BOOKING = new Booking(2L, LocalDateTime.now().plusDays(2),
            LocalDateTime.now().plusDays(5), BookingStatus.WAITING, Item.builder().id(1L).build(), BOOKER);
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository requestRepository;
    @InjectMocks
    private ItemServiceImpl itemService;

    @BeforeEach
    void init() {
        owner = new User(1L, "Mike", "mike@gmail.com");
        requestor = new User(2L, "Eric", "eric@gmail.com");
        request = new ItemRequest(1L, "Torque screwdriver", LocalDateTime.now(), requestor);
        item = Item.builder()
                .id(1L)
                .name("Screwdriver")
                .description("Torque screwdriver")
                .available(true)
                .owner(owner)
                .request(request)
                .build();
        comment = new Comment(1L, "Good item!", LocalDateTime.now(), item, owner);
    }

    @AfterEach
    void tearDown() {
        owner = null;
        requestor = null;
        request = null;
        item = null;
        comment = null;
    }

    @ParameterizedTest
    @MethodSource("getBookings")
    void findById(Booking lastBooking, Booking nextBooking) {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findAllByItemOwnerId(owner.getId(), Sort.by(Sort.Direction.DESC, "start")))
                .thenReturn(lastBooking == null || nextBooking == null ? List.of() : List.of(lastBooking, nextBooking));

        ItemDto dto = itemService.getItemById(owner.getId(), item.getId());

        assertEquals(dto.getId(), item.getId());
        assertEquals(dto.getName(), item.getName());
        assertEquals(dto.getDescription(), item.getDescription());
        assertEquals(dto.getAvailable(), item.getAvailable());

        if (lastBooking == null || nextBooking == null) {
            assertNull(dto.getLastBooking());
            assertNull(dto.getNextBooking());
        } else {
            assertSoftly(softAssertions -> {
                softAssertions.assertThat(dto.getLastBooking())
                        .usingRecursiveComparison()
                        .isEqualTo(BookingMapper.mapToBookingDtoWithIds(lastBooking));
            });
            assertSoftly(softAssertions -> {
                softAssertions.assertThat(dto.getNextBooking())
                        .usingRecursiveComparison()
                        .isEqualTo(BookingMapper.mapToBookingDtoWithIds(nextBooking));
            });
        }

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findAllByItemOwnerId(owner.getId(), Sort.by(Sort.Direction.DESC, "start"));
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findByNotValidUserId(Long userId) {
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            itemService.getItemById(userId, item.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Пользователь с id=%d не найден!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findByNotValidId(Long id) {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class, () -> {
            itemService.getItemById(owner.getId(), id);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Вещь с id=%d не найдена!", id);

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "Turbo drill", "DRILL", "2000"})
    void findAllByText(String text) {
        Pageable pageable = PageRequest.of(0, 10);
        Mockito.when(itemRepository.search(text, pageable)).thenReturn(text.isBlank() ? new PageImpl<>(Collections.emptyList()) : new PageImpl<>(List.of(item)));

        List<Item> items = itemService.searchItem(text, pageable);

        if (text.isBlank()) {
            assertEquals(0, items.size());
        } else {
            assertEquals(1, items.size());
        }

        Mockito.verify(itemRepository, Mockito.times(1)).search(text, pageable);
    }

    @Test
    void findAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findAllByOwnerId(owner.getId(), pageable)).thenReturn(List.of(item));

        List<ItemDto> items = itemService.getItems(owner.getId(), 0, 10);

        assertEquals(items.size(), 1);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findAllByOwnerId(owner.getId(), pageable);
    }

    @Test
    void findAllByNotValidUserId() {
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            itemService.getItems(owner.getId(), 0, 10);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Пользователь с id=%d не найден!", owner.getId());

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void save() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(item);

        ItemDto dto = ItemMapper.mapToItemDto(item, request);
        ItemDto savedDto = itemService.addNewItem(owner.getId(), dto);

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(dto)
                    .usingRecursiveComparison()
                    .isEqualTo(savedDto);
        });

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void saveByNotValidUserId() {
        ItemDto dto = ItemMapper.mapToItemDto(item, request);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            itemService.addNewItem(owner.getId(), dto);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Пользователь с id=%d не найден!", owner.getId());

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void saveByNotValidRequestId() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        ItemDto dto = ItemMapper.mapToItemDto(item, request);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            itemService.addNewItem(owner.getId(), dto);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Item request with id=%d not found!", request.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @Test
    void update() {
        Item newItem = Item.builder()
                .id(item.getId())
                .name("Saw")
                .description("Electric saw")
                .available(false)
                .owner(owner)
                .request(request)
                .build();

        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ItemDto dto = ItemMapper.mapToItemDto(newItem, request);
        ItemDto savedItem = itemService.updateItem(owner.getId(), newItem.getId(), dto);

        assertEquals(savedItem.getId(), newItem.getId());
        assertEquals(savedItem.getName(), newItem.getName());
        assertEquals(savedItem.getDescription(), newItem.getDescription());
        assertEquals(savedItem.getAvailable(), newItem.getAvailable());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
    }

    @Test
    void updateByNotFoundUserId() {
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            ItemDto dto = ItemMapper.mapToItemDto(item, request);
            itemService.updateItem(owner.getId(), dto.getId(), dto);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Пользователь с id=%d не найден!", owner.getId());

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void updateByNotFoundItemId() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class, () -> {
            ItemDto dto = ItemMapper.mapToItemDto(item, request);
            itemService.updateItem(owner.getId(), dto.getId(), dto);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Вещь с id=%d не найдена!", item.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @Test
    void updateByNotOwner() {
        Item newItem = Item.builder()
                .id(item.getId())
                .name("Saw")
                .description("Electric saw")
                .available(false)
                .owner(owner)
                .request(request)
                .build();

        Mockito.when(userRepository.findById(requestor.getId())).thenReturn(Optional.of(requestor));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            ItemDto dto = ItemMapper.mapToItemDto(newItem, request);
            itemService.updateItem(requestor.getId(), newItem.getId(), dto);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("У пользователя c id=%d отсутствуют права на изменение вещи!", requestor.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(requestor.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
    }

    @Test
    void deleteItemById() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        itemService.deleteItem(owner.getId(), item.getId());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void deleteItemByNotFoundUserId(Long userId) {
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            itemService.deleteItem(userId, item.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Пользователь с id=%d не найден!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void deleteItemByNotFoundId(Long id) {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class, () -> {
            itemService.deleteItem(owner.getId(), id);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Вещь с id=%d не найдена!", id);

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @Test
    void addComment() {
        Mockito.when(userRepository.findById(BOOKER.getId())).thenReturn(Optional.of(BOOKER));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findAllByItemId(item.getId())).thenReturn(List.of(LAST_BOOKING));
        Mockito.when(commentRepository.save(Mockito.any())).thenReturn(comment);

        CommentDto dto = CommentMapper.mapToCommentDto(comment);
        CommentWithInfoDto savedDto = itemService.addComment(dto, BOOKER.getId(), item.getId());

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(dto)
                    .usingRecursiveComparison()
                    .isEqualTo(savedDto);
        });

        Mockito.verify(userRepository, Mockito.times(1)).findById(BOOKER.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findAllByItemId(item.getId());
        Mockito.verify(commentRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void saveCommentByNotFoundUserId() {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            CommentDto dto = CommentMapper.mapToCommentDto(comment);
            itemService.addComment(dto, owner.getId(), item.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Пользователь с id=%d не найден!", owner.getId());

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void saveCommentByNotFoundItemId() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            CommentDto dto = CommentMapper.mapToCommentDto(comment);
            itemService.addComment(dto, owner.getId(), item.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Вещь с id=%d не найдена!", item.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    private static Stream<Arguments> getBookings() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(LAST_BOOKING, NEXT_BOOKING)
        );
    }
}
