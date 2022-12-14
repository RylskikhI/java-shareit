package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public List<Item> getItems(long userId) {
        return itemRepository.findByUserId(userId);
    }

    @Override
    public Item addNewItem(long userId, ItemDto itemDto) {
        return itemRepository.save(userId, itemDto);
    }

    @Override
    public void deleteItem(long userId, long itemId) {
        itemRepository.deleteByUserIdAndItemId(userId, itemId);
    }

    @Override
    public Item updateItem(long userId, long itemId, Item item) {
        return itemRepository.update(userId, itemId, item);
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemRepository.getById(itemId);
    }

    @Override
    public List<Item> searchItem(String text) {
        return itemRepository.searchItem(text);
    }
}
