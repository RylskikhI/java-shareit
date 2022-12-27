package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.exception.SecurityException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentWithInfoDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserService userService,
                           UserRepository userRepository, BookingRepository bookingRepository, CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public List<ItemDto> getItems(long userId) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        Comparator<ItemDto> comparator = (o1, o2) -> {
            final BookingDto lb1 = o1.getLastBooking();
            final BookingDto lb2 = o2.getLastBooking();

            if (lb1 == null && lb2 == null) {
                return 0;
            }
            if (lb1 == null) {
                return 1;
            }
            if (lb2 == null) {
                return -1;
            }
            return lb1.getStart().compareTo(lb2.getStart());
        };
        return itemRepository.findAllByOwnerId(userWrap.getId()).stream()
                .map(it -> ItemMapper.mapToItemDto(it, commentRepository.findAllByItemId(it.getId())))
                .peek(it -> {
                    final List<Booking> bookings = bookingRepository.findAllByItemId(it.getId());
                    final Booking lastBooking = findBookingByStatePastOrFuture(BookingState.PAST, bookings);
                    final Booking nextBooking = findBookingByStatePastOrFuture(BookingState.FUTURE, bookings);
                    if (lastBooking != null && nextBooking != null) {
                        it.setLastBooking(BookingMapper.mapToBookingDtoWithIds(lastBooking));
                        it.setNextBooking(BookingMapper.mapToBookingDtoWithIds(nextBooking));
                    }
                })
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ItemDto addNewItem(Long id, ItemDto itemDto) {
        if (itemValidation(itemDto) && isUserPresent(id)) {
            User user = userRepository.findById(id).orElseThrow(
                    () -> new EntityNotFoundException(String.format("User with id=%d not found!", id))
            );
            Item item = itemRepository.save(ItemMapper.mapToItem(itemDto, user));
            Set<Comment> comments = commentRepository.findAllByItemId(item.getId());
            return ItemMapper.mapToItemDto(item, comments);
        } else {
            throw new UserNotFoundException("Пользователь не существует");
        }
    }

    @Override
    public void deleteItem(long userId, long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Transactional
    @Override
    public ItemDto updateItem(long userId, long itemId, Item item) {
        Item itemForUpdate = itemRepository.findById(itemId).orElseThrow(
                () -> new ItemNotFoundException("Вещь не найдена"));

        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("Пользователь не найден"));

        if (user.getId().equals(itemForUpdate.getOwner().getId())) {
            if (item.getName() != null) {
                itemForUpdate.setName(item.getName());
            }
            if (item.getDescription() != null) {
                itemForUpdate.setDescription(item.getDescription());
            }
            if (item.getAvailable() != null) {
                itemForUpdate.setAvailable(item.getAvailable());
            }

        } else {
            throw new SecurityException("У пользователя отсутствуют права на изменение вещи");
        }
        final Set<Comment> comments = commentRepository.findAllByItemId(itemForUpdate.getId());
        itemRepository.save(itemForUpdate);
        return ItemMapper.mapToItemDto(itemForUpdate, comments);
    }

    @Override
    public ItemDto getItemById(Long userId, Long itemId) {

        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new ItemNotFoundException("Вещь не найдена"));

        final List<Booking> bookings = bookingRepository.findAllByItemOwnerId(user.getId(), Sort.by(Sort.Direction.DESC, "start"));
        final Booking lastBooking = findBookingByStatePastOrFuture(BookingState.PAST, bookings);
        final Booking nextBooking = findBookingByStatePastOrFuture(BookingState.FUTURE, bookings);
        final Set<Comment> comments = commentRepository.findAllByItemId(item.getId());
        return lastBooking == null || nextBooking == null ?
                ItemMapper.mapToItemDto(item, comments) :
                ItemMapper.mapToItemDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<Item> searchItem(String text) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }

        return itemRepository.search(text);
    }

    @Override
    @Transactional
    public CommentWithInfoDto addComment(CommentDto commentDto, Long userId, Long id) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        Item item = itemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Item with id=%d not found!", id))
        );

        checkIfBookedItem(id, userId);

        final Comment comment = CommentMapper.mapToComment(commentDto, item, user);
        final Comment commentWrap = commentRepository.save(comment);
        return CommentMapper.mapToCommentInfoDto(commentWrap);
    }

    private Booking findBookingByStatePastOrFuture(BookingState state, List<Booking> bookings) {
        final LocalDateTime currentTime = LocalDateTime.now();

        switch (state) {
            case PAST: {
                return bookings.stream()
                        .filter(it -> it.getStart().isBefore(currentTime))
                        .findFirst().orElse(null);
            }
            case FUTURE: {
                return bookings.stream()
                        .filter(it -> it.getStart().isAfter(currentTime))
                        .findFirst().orElse(null);
            }
            default: {
                throw new BookingStateExistsException("Unknown state: UNSUPPORTED_STATUS");
            }
        }
    }

    private void checkIfBookedItem(Long itemId, Long userId) throws CommentException {
        List<Booking> bookings = bookingRepository.getBookingsByItemAndBooker(userId, itemId);
        if (bookings.size() != 0) {
            if (bookings.stream().noneMatch(t -> t.getEnd().isBefore(LocalDateTime.now()))) {
                throw new CommentException("Дата бронирования еще не наступила");
            }
        } else {
            throw new CommentException("Пользователь не брал вещь в аренду!");
        }
    }

    private boolean itemValidation(ItemDto itemDto) {
        boolean isValidated = true;

        if (!itemDto.getAvailable()) {
            isValidated = false;
            log.warn("Вещь недоступна");
        }
        return isValidated;
    }

    private boolean isUserPresent(long userId) {
        for (UserDto userDto : userService.getAllUsers()) {
            if (userDto.getId() == userId) {
                return true;
            }
        }
        return false;
    }


}
