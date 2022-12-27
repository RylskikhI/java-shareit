package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentWithInfoDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto addNewItem(Long id, ItemDto itemDto);

    List<ItemDto> getItems(long userId);

    void deleteItem(long userId, long itemId);

    ItemDto updateItem(long userId, long itemId, Item item);

    ItemDto getItemById(Long userId, Long itemId);

    List<Item> searchItem(String text);

    CommentWithInfoDto addComment(CommentDto commentDto, Long userId, Long id);
}
