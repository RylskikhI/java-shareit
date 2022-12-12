package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.SecurityException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private final UserService userService;

    private final Map<Long, Item> items = new HashMap<>();

    private long id = 0;

    @Override
    public List<Item> findByUserId(long userId) {
        return items.values().stream()
                .filter(t -> t.getOwner().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Item getById(Long itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> searchItem(String text) {
        String regExp = ".*" + text.toLowerCase() + ".*";
        if (text.equals("")) {
            return Collections.emptyList();
        } else {
            return items.values().stream()
                    .filter(t -> (t.getName().toLowerCase().matches(regExp) ||
                            t.getDescription().toLowerCase().matches(regExp)) && t.getAvailable())
                    .collect(Collectors.toList());
        }
    }


    @Override
    public Item save(long userId, ItemDto itemDto) {
        if (itemValidation(itemDto) && isUserPresent(userId)) {
            itemDto.setId(++id);
            itemDto.setOwner(userId);
            items.put(itemDto.getId(), ItemMapper.toItem(itemDto));
        } else {
            throw new UserNotFoundException("Проверьте корректность введённых данных");
        }
        return ItemMapper.toItem(itemDto);
    }

    @Override
    public void deleteByUserIdAndItemId(long userId, long itemId) {
        if (items.containsKey(userId)) {
            items.remove(itemId);
        }
    }

    @Override
    public Item update(long userId, long itemId, Item item) {
        Item itemForUpdate = items.get(itemId);

        if (!getById(itemId).getOwner().equals(userId)) {
            throw new SecurityException("У пользователя отсутствуют права на изменение товара");
        } else {
            if (item.getName() != null) {
                itemForUpdate.setName(item.getName());
            }
            if (item.getDescription() != null) {
                itemForUpdate.setDescription(item.getDescription());
            }
            if (item.getAvailable() != null) {
                itemForUpdate.setAvailable(item.getAvailable());
            }
        }
        return itemForUpdate;
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
        for (UserDto user : userService.getAllUsers()) {
            if (user.getId() == userId) {
                return true;
            }
        }
        return false;
    }
}
