package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    List<Item> findByUserId(long userId);

    Item save(long userId, ItemDto itemDto);

    void deleteByUserIdAndItemId(long userId, long itemId);

    Item update(long userId, long itemId, Item item);

    Item getById(Long itemId);

    List<Item> searchItem(String text);
}
