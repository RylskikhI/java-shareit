package ru.practicum.shareit.item.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentWithInfoDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    /**
     * Добавление новой вещи.
     * @param itemDto Entity dto.
     * @param id User id, владелец(owner) вещи.
     * @return ItemDto.
     */
    ItemDto addNewItem(Long id, ItemDto itemDto);

    /**
     * Найти все вещи пользователя по id.
     * @param userId User id, владелец(owner) вещи.
     * @return List<ItemDto>.
     */
    List<ItemDto> getItems(long userId, int page, int size);

    /**
     * Удалить вещь по id. Только владелец(owner) может сделать это.
     * @param userId User id, владелец(owner) вещи.
     * @param itemId Item id.
     */
    void deleteItem(long userId, long itemId);

    /**
     * Обновить вещь по id. Только владелец(owner) может сделать это.
     * @param itemDto Entity dto.
     * @param userId User id, владелец(owner) вещи.
     * @param itemId Item id.
     * @return ItemDto.
     */
    ItemDto updateItem(long userId, long itemId, ItemDto itemDto);

    /**
     * Найти вещь по id.
     * @param userId User id, владелец(owner) вещи.
     * @param itemId Item id.
     * @return ItemDto.
     */
    ItemDto getItemById(Long userId, Long itemId);

    /**
     * Поиск вещей по ключевому слову.
     * @param text ключевое слово.
     * @return List<Item>.
     */
    List<Item> searchItem(String text, Pageable pageable);

    /**
     * Добавить комментарий после бронирования.
     * @param commentDto Entity dto.
     * @param userId User id.
     * @param id Comment id.
     * @return CommentWithInfoDto.
     */
    CommentWithInfoDto addComment(CommentDto commentDto, Long userId, Long id);
}
