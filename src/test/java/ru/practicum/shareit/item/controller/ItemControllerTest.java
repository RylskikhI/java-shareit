package ru.practicum.shareit.item.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mockito.Mockito;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentWithInfoDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
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
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.utils.LocalDateTimeTypeAdapter;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    private UserDto owner;
    private UserDto booker;
    private Item item;
    private ItemDto dto;
    private Comment comment;
    private CommentWithInfoDto commentInfoDto;
    private Gson gson;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemService itemService;

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
        comment = new Comment(1L, "Good drill!", LocalDateTime.now(), item, UserMapper.mapToUser(booker));
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .create();
        dto = ItemMapper.mapToItemDto(item);
        commentInfoDto = CommentMapper.mapToCommentInfoDto(comment);
    }

    @AfterEach
    void tearDown() {
        owner = null;
        booker = null;
        item = null;
        comment = null;
        gson = null;
        dto = null;
        commentInfoDto = null;
    }

    @Test
    @DisplayName("Send GET request /items/{id}")
    void findById() throws Exception {
        Mockito.when(itemService.getItemById(owner.getId(), item.getId())).thenReturn(dto);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber());


        Mockito.verify(itemService, Mockito.times(1)).getItemById(owner.getId(), item.getId());
    }

    @Test
    @DisplayName("Send GET request /items/{id}")
    void findByNotValidId() throws Exception {
        Mockito.when(itemService.getItemById(owner.getId(), item.getId())).thenThrow(ItemNotFoundException.class);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(itemService, Mockito.times(1)).getItemById(owner.getId(), item.getId());
    }

    @Test
    @DisplayName("Send GET request /items")
    void findAll() throws Exception {
        Mockito.when(itemService.getItems(owner.getId(), 0, 10)).thenReturn(List.of(dto));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/items")
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());

        Mockito.verify(itemService, Mockito.times(1)).getItems(owner.getId(), 0, 10);
    }

    @Test
    @DisplayName("Send POST request /items")
    void save() throws Exception {
        dto = ItemMapper.mapToItemDto(item, new ItemRequest());
        Mockito.when(itemService.addNewItem(Mockito.anyLong(), Mockito.any())).thenReturn(dto);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/items")
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(dto)))
                .andExpect(status().isOk());

        Mockito.verify(itemService, Mockito.times(1)).addNewItem(Mockito.anyLong(), Mockito.any());
    }

    @Test
    @DisplayName("Send POST request /items/{id}/comment")
    void saveComment() throws Exception {
        CommentDto commentDto = CommentMapper.mapToCommentDto(comment);
        Mockito.when(itemService.addComment(Mockito.any(), Mockito.anyLong(), Mockito.anyLong())).thenReturn(commentInfoDto);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/items/{id}/comment", item.getId())
                        .header("X-Sharer-User-Id", booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(commentDto)))
                .andExpect(status().isOk());

        Mockito.verify(itemService, Mockito.times(1)).addComment(Mockito.any(), Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @DisplayName("Send POST request /items/{id}/comment")
    void saveCommentByNotBooker() throws Exception {
        CommentDto commentDto = CommentMapper.mapToCommentDto(comment);
        Mockito.when(itemService.addComment(Mockito.any(), Mockito.anyLong(), Mockito.anyLong())).thenThrow(ValidationException.class);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/items/{id}/comment", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(commentDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, Mockito.times(1)).addComment(Mockito.any(), Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @DisplayName("Send PATCH request /items/{id}")
    void update() throws Exception {
        dto.setName("Drill 2000");
        dto.setAvailable(false);
        dto.setDescription("Very good drill!");
        Mockito.when(itemService.updateItem(Mockito.anyLong(), Mockito.anyLong(), Mockito.any())).thenReturn(dto);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(dto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Drill 2000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.available").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Very good drill!"));

        Mockito.verify(itemService, Mockito.times(1)).updateItem(Mockito.anyLong(), Mockito.anyLong(), Mockito.any());
    }

    @Test
    @DisplayName("Send PATCH request /items/{id}")
    void updateByNotOwner() throws Exception {
        Mockito.when(itemService.updateItem(Mockito.anyLong(), Mockito.anyLong(), Mockito.any())).thenThrow(UserNotFoundException.class);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .patch("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(dto)))
                .andExpect(status().isNotFound());

        Mockito.verify(itemService, Mockito.times(1)).updateItem(Mockito.anyLong(), Mockito.anyLong(), Mockito.any());
    }

    @Test
    @DisplayName("Send DELETE request items/{id}")
    void deleteById() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(itemService, Mockito.times(1)).deleteItem(owner.getId(), item.getId());
    }
}
