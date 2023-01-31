package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

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
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           CommentRepository commentRepository,
                           ItemRequestRepository itemRequestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Override
    public List<ItemDto> getItems(long userId, int page, int size) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("Пользователь с id=%d не найден!", userId))
        );
        Pageable pageable = PageRequest.of(page, size);
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
        return itemRepository.findAllByOwnerId(user.getId(), pageable).stream()
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
        if (itemValidation(itemDto)) {
            User user = userRepository.findById(id).orElseThrow(
                    () -> new EntityNotFoundException(String.format("Пользователь с id=%d не найден!", id))
            );
            ItemRequest request = null;
            if (itemDto.getRequestId() != null) {
                request =  itemRequestRepository.findById(itemDto.getRequestId()).orElseThrow(
                        () -> new EntityNotFoundException(String.format("Item request with id=%d not found!", itemDto.getRequestId()))
                );
            }
            Item item = request == null ? ItemMapper.mapToItem(itemDto, user) : ItemMapper.mapToItem(itemDto, user, request);
            Item itemToSave = itemRepository.save(item);
            Set<Comment> comments = commentRepository.findAllByItemId(item.getId());
            return item.getRequest() == null ? ItemMapper.mapToItemDto(itemToSave, comments) : ItemMapper.mapToItemDto(itemToSave, itemToSave.getRequest());
        } else {
            throw new UserNotFoundException("Пользователь не существует");
        }
    }

    @Transactional
    @Override
    public void deleteItem(long userId, long itemId) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("Пользователь с id=%d не найден!", userId))
        );
        final Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new ItemNotFoundException(String.format("Вещь с id=%d не найдена!", itemId))
        );
        itemRepository.deleteById(item.getId());
    }

    @Transactional
    @Override
    public ItemDto updateItem(ItemDto itemDto, Long userId, Long id) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("Пользователь с id=%d не найден!", userId)));

        Item itemForUpdate = itemRepository.findById(id).orElseThrow(
                () -> new ItemNotFoundException(String.format("Вещь с id=%d не найдена!", id)));

        if (user.getId().equals(itemForUpdate.getOwner().getId())) {
            if (itemDto.getName() != null) {
                itemForUpdate.setName(itemDto.getName());
            }
            if (itemDto.getDescription() != null) {
                itemForUpdate.setDescription(itemDto.getDescription());
            }
            if (itemDto.getAvailable() != null) {
                itemForUpdate.setAvailable(itemDto.getAvailable());
            }

        } else {
            throw new SecurityException(String.format("У пользователя c id=%d отсутствуют права на изменение вещи!", userId));
        }
        final Set<Comment> comments = commentRepository.findAllByItemId(itemForUpdate.getId());
        itemRepository.save(itemForUpdate);
        return ItemMapper.mapToItemDto(itemForUpdate, comments);
    }

    @Override
    public ItemDto getItemById(Long userId, Long itemId) {

        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("Пользователь с id=%d не найден!", userId)));

        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new ItemNotFoundException(String.format("Вещь с id=%d не найдена!", itemId)));

        final List<Booking> bookings = bookingRepository.findAllByItemOwnerId(user.getId(), Sort.by(Sort.Direction.DESC, "start"));
        final Booking lastBooking = findBookingByStatePastOrFuture(BookingState.PAST, bookings);
        final Booking nextBooking = findBookingByStatePastOrFuture(BookingState.FUTURE, bookings);
        final Set<Comment> comments = commentRepository.findAllByItemId(item.getId());
        return lastBooking == null || nextBooking == null ?
                ItemMapper.mapToItemDto(item, comments) :
                ItemMapper.mapToItemDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<Item> searchItem(String text, Pageable pageable) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }

        Page<Item> itemPage = itemRepository.search(text, pageable);
        return itemPage.getContent();
    }

    @Override
    @Transactional
    public CommentWithInfoDto addComment(CommentDto commentDto, Long userId, Long id) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id=%d не найден!", userId))
        );
        final Item item = itemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Вещь с id=%d не найдена!", id))
        );
        final List<Booking> bookings = bookingRepository.findAllByItemId(item.getId());

        if (bookings.size() != 0) {
            if (bookings.stream().noneMatch(t -> t.getEnd().isBefore(LocalDateTime.now()))) {
                throw new BookingStatusException("Дата бронирования еще не наступила");
            }
        } else {
            throw new ValidationException(String.format("Пользователь userId=%d не брал вещь в аренду!", userId));
        }

        final Comment comment = CommentMapper.mapToComment(commentDto, item, user);
        final Comment commentToAdd = commentRepository.save(comment);
        return CommentMapper.mapToCommentInfoDto(commentToAdd);
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

    private boolean itemValidation(ItemDto itemDto) {
        boolean isValidated = true;

        if (!itemDto.getAvailable()) {
            isValidated = false;
            log.warn("Вещь недоступна");
        }
        return isValidated;
    }
}
