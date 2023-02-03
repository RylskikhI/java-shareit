package ru.practicum.shareit.request.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final RequestService requestService;

    @Autowired
    public ItemRequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ItemRequestDto addNewRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return requestService.addNewRequest(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getRequestsMadeByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.getRequestsMadeByOwner(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getRequestsMadeByOthers(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                        @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                                        @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        return requestService.getRequestsMadeByOthers(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long requestId) {
        return requestService.getRequestById(userId, requestId);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        requestService.deleteById(userId, id);
    }

}
