package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item addNewItem(long userId, ItemDto itemDto);
    List<Item> getItems(long userId);
    void deleteItem(long userId, long itemId);
    Item updateItem(long userId, long itemId, Item item);
    Item getItemById(Long itemId);
    List<Item> searchItem(String text);
}
