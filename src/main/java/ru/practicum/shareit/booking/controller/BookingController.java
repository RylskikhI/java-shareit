package ru.practicum.shareit.booking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithEntities;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDtoWithEntities createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @Valid @RequestBody BookingDto bookingDto) {
        return bookingService.createBooking(userId, bookingDto);
    }

    @GetMapping("/{id}")
    public BookingDtoWithEntities findById(@RequestHeader("X-Sharer-User-Id") Long userId,
                               @PathVariable Long id) {
        return bookingService.findById(userId, id);
    }

    @GetMapping
    public List<BookingDtoWithEntities> findAllByBookerId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam(name = "state", defaultValue = "ALL") String state) {
        return bookingService.findAllByBookerId(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDtoWithEntities> findAllByItemOwnerId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @RequestParam(name = "state", defaultValue = "ALL") String state) {
        return bookingService.findAllByItemOwnerId(userId, state);
    }

    @PatchMapping("/{id}")
    public BookingDtoWithEntities update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @Valid @PathVariable Long id,
                                 @RequestParam(name = "approved") String approved) {
        return bookingService.update(userId, id, approved);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @PathVariable Long id) {
        bookingService.deleteById(userId, id);
    }
}
