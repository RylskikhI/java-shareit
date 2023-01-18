package ru.practicum.shareit.request.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.MyPageRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.*;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final ItemRequestRepository itemRequestRepository;

    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    @Autowired
    public RequestServiceImpl(ItemRequestRepository itemRequestRepository,
                              ItemRepository itemRepository,
                              UserRepository userRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public ItemRequestDto addNewRequest(Long userId, ItemRequestDto itemRequestDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("Пользователь с id=%d не найден!", userId))
        );

        if (itemRequestDto.getDescription() == null) {
            throw new ValidationException("Отсутствует описание в запросе");
        }

        ItemRequest itemRequest = itemRequestRepository.save(ItemRequestMapper.mapToItemRequest(itemRequestDto, user));
        return ItemRequestMapper.mapToItemRequestDto(itemRequest);

    }

    @Override
    public List<ItemRequestDto> getRequestsMadeByOwner(Long userId) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final List<ItemRequest> requests = itemRequestRepository.findAllByRequestorId(user.getId());

        Map<Long, Set<Item>> items = itemRepository.findItemByRequestIn(requests).stream()
                .collect(groupingBy(item -> item.getRequest().getId(), toSet()));

        return itemRequestRepository.findAllByRequestorId(user.getId()).stream()
                .map(it -> ItemRequestMapper.mapToItemRequestDto(it, items.get(it.getId())))
                .collect(toList());
    }

    @Override
    public List<ItemRequestDto> getRequestsMadeByOthers(Long userId, Integer from, Integer size) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final MyPageRequest pageRequest = new MyPageRequest(from, size, Sort.by(DESC, "created"));
        final List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNot(user.getId(), pageRequest);

        Map<Long, Set<Item>> items = itemRepository.findItemByRequestIn(requests).stream()
                .collect(groupingBy(item -> item.getRequest().getId(), toSet()));

        return itemRequestRepository.findAllByRequestorIdNot(user.getId(), pageRequest).stream()
                .map(it -> ItemRequestMapper.mapToItemRequestDto(it, items.get(it.getId())))
                .collect(toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final ItemRequest request = itemRequestRepository.findById(requestId).orElseThrow(
                () -> new ItemNotFoundException(String.format("Item request with id=%d not found!", requestId))
        );
        final Set<Item> items = itemRepository.findByRequestId(request.getId());
        return ItemRequestMapper.mapToItemRequestDto(request, items);
    }

    @Override
    @Transactional
    public void deleteById(Long userId, Long id) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final ItemRequest request = itemRequestRepository.findById(id).orElseThrow(
                () -> new ItemNotFoundException(String.format("Item request with id=%d not found!", id))
        );
        itemRequestRepository.deleteById(request.getId());
    }
}
