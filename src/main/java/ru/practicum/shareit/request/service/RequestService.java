package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface RequestService {
    ItemRequestDto addNewRequest(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestDto> getRequestsMadeByOwner(Long userId);

    List<ItemRequestDto> getRequestsMadeByOthers(Long userId, Integer from, Integer size);

    ItemRequestDto getRequestById(Long userId, Long requestId);

    void deleteById(Long userId, Long id);

}
