package ru.practicum.shareit.booking.controller;

import java.util.List;
import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mockito.Mockito;
import ru.practicum.shareit.booking.dto.BookingDtoWithEntities;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.exception.BookingStateExistsException;
import ru.practicum.shareit.utils.LocalDateTimeAdapter;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    private UserDto owner;
    private UserDto booker;
    private Item item;
    private Booking booking;
    private BookingDto dto;
    private BookingDtoWithEntities bookingDtoWithEntities;
    private Gson gson;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingService bookingService;

    @BeforeEach
    void init() {
        owner = new UserDto(1L, "Nikolas", "nik@mail.ru");
        booker = new UserDto(2L, "Djon", "djony@mail.ru");
        item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .owner(UserMapper.mapToUser(owner))
                .build();
        booking = new Booking(1L, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5),
                BookingStatus.WAITING, item, UserMapper.mapToUser(booker));
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .serializeNulls()
                .create();
        dto = BookingMapper.mapToBookingDtoWithIds(booking);
        bookingDtoWithEntities = BookingMapper.mapToBookingDtoWithEntities(booking);
    }

    @AfterEach
    void tearDown() {
        owner = null;
        booker = null;
        item = null;
        booking = null;
        gson = null;
        dto = null;
        bookingDtoWithEntities = null;
    }

    @Test
    @DisplayName("Send GET request /bookings/{id}")
    void findById() throws Exception {
        Mockito.when(bookingService.findBookingById(owner.getId(), booking.getId())).thenReturn(bookingDtoWithEntities);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/bookings/{id}", booking.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber());

        Mockito.verify(bookingService, Mockito.times(1)).findBookingById(owner.getId(), booking.getId());
    }

    @Test
    @DisplayName("Send GET request /bookings/{id}")
    void findByNotValidId() throws Exception {
        Mockito.when(bookingService.findBookingById(owner.getId(), booking.getId())).thenThrow(UserNotFoundException.class);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/bookings/{id}", booking.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(bookingService, Mockito.times(1)).findBookingById(owner.getId(), booking.getId());
    }

    @Test
    @DisplayName("Send GET request /bookings?state={state}&from={from}&size={size}")
    void findAllByBookerId() throws Exception {
        Mockito.when(bookingService.findAllByBookerId(booker.getId(), "ALL", 0, 10)).thenReturn(List.of(bookingDtoWithEntities));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/bookings?state={state}&from={from}&size={size}", BookingState.ALL, 0, 10)
                        .header("X-Sharer-User-Id", booker.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());

        Mockito.verify(bookingService, Mockito.times(1)).findAllByBookerId(booker.getId(), "ALL", 0, 10);
    }

    @Test
    @DisplayName("Send GET request /bookings?state={state}&from={from}&size={size}")
    void findAllByBookerIdAndNotValidState() throws Exception {
        Mockito.when(bookingService.findAllByBookerId(booker.getId(), "PPS", 0, 10)).thenThrow(BookingStateExistsException.class);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/bookings?state={state}&from={from}&size={size}", "PPS", 0, 10)
                        .header("X-Sharer-User-Id", booker.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingService, Mockito.times(1)).findAllByBookerId(booker.getId(), "PPS", 0, 10);
    }

    @Test
    @DisplayName("Send GET request /bookings/owner?state={state}&from={from}&size={size}")
    void findAllByItemOwnerId() throws Exception {
        Mockito.when(bookingService.findAllByItemOwnerId(owner.getId(), "ALL", 0, 10)).thenReturn(List.of(bookingDtoWithEntities));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/bookings/owner?state={state}&from={from}&size={size}", BookingState.ALL, 0, 10)
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());

        Mockito.verify(bookingService, Mockito.times(1)).findAllByItemOwnerId(owner.getId(), "ALL", 0, 10);
    }

    @Test
    @DisplayName("Send GET request /bookings/owner?state={state}&from={from}&size={size}")
    void findAllByItemOwnerIdAndNotValidState() throws Exception {
        Mockito.when(bookingService.findAllByItemOwnerId(owner.getId(), "PPS", 0, 10)).thenThrow(BookingStateExistsException.class);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/bookings/owner?state={state}&from={from}&size={size}", "PPS", 0, 10)
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingService, Mockito.times(1)).findAllByItemOwnerId(owner.getId(), "PPS", 0, 10);
    }

    @Test
    @DisplayName("Send POST request /bookings")
    void save() throws Exception {
        dto.setStart(booking.getStart().plusDays(15));
        dto.setEnd(booking.getEnd().plusDays(10));
        Mockito.when(bookingService.createBooking(Mockito.anyLong(), Mockito.any())).thenReturn(bookingDtoWithEntities);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/bookings")
                        .header("X-Sharer-User-Id", booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(dto)))
                .andExpect(status().isOk());

        Mockito.verify(bookingService, Mockito.times(1)).createBooking(Mockito.anyLong(), Mockito.any());
    }

    @Test
    @DisplayName("Send PATCH request /bookings/{id}?approved={approved}")
    void update() throws Exception {
        bookingDtoWithEntities.setStatus(BookingStatus.REJECTED);
        Mockito.when(bookingService.updateBooking(owner.getId(), booking.getId(), false)).thenReturn(bookingDtoWithEntities);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/bookings/{id}?approved={approved}", booking.getId(), false)
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(BookingStatus.REJECTED.name()));

        Mockito.verify(bookingService, Mockito.times(1)).updateBooking(owner.getId(), booking.getId(), false);
    }

    @Test
    @DisplayName("Send PATCH request /bookings/{id}?approved={approved}")
    void updateByNotValidStatus() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/bookings/{id}?approved={approved}", booking.getId(), null)
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Send DELETE request /bookings/{id}")
    void deleteById() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/bookings/{id}", booking.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(bookingService, Mockito.times(1)).deleteBookingById(owner.getId(), booking.getId());
    }
}