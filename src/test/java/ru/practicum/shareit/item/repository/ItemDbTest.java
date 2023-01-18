package ru.practicum.shareit.item.repository;

import java.util.List;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.TypedQuery;
import javax.persistence.EntityManager;

import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemDbTest {
    private User owner;
    private User requestor;
    private Item item;
    private ItemRequest request;
    private final EntityManager em;
    private final UserService userService;
    private final ItemService itemService;
    private final RequestService requestService;

    @BeforeEach
    void init() {
        owner = new User(null, "Maks", "maks@mail.ru");
        requestor = new User(null, "Bob", "bob@mail.ru");
        request = new ItemRequest(null, "Good drill!", LocalDateTime.now(), requestor);
        item = Item.builder()
                .name("Drill")
                .description("Drill MaxPro 2000")
                .available(true)
                .owner(owner)
                .request(request)
                .build();
    }

    @AfterEach
    void tearDown() {
        owner = null;
        requestor = null;
        request = null;
        item = null;
    }

    @Test
    void findById() {
        ItemDto dto = makeItem(item);
        TypedQuery<Long> query = em.createQuery("select i.id from Item as i where i.id = :id", Long.class);
        Long id = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        dto = itemService.getItemById(owner.getId(), id);

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getName()).isEqualTo(item.getName());
        assertThat(dto.getDescription()).isEqualTo(item.getDescription());
        assertThat(dto.getAvailable()).isEqualTo(item.getAvailable());
    }

    @Test
    void findAll() {
        ItemDto dto = makeItem(item);
        TypedQuery<Item> query = em.createQuery("select i from Item as i where i.owner.id = :id", Item.class);
        List<Item> result = query
                .setParameter("id", owner.getId())
                .getResultList();

        List<ItemDto> items = itemService.getItems(owner.getId(), 0, 10);

        assertThat(result.size()).isEqualTo(items.size());
    }

    @Test
    void findAllByText() {
        ItemDto dto = makeItem(item);
        Pageable pageable = PageRequest.of(0, 10);
        String text = "Drill";
        TypedQuery<Item> query = em.createQuery("select i from Item as i where i.available = true and " +
                "lower(i.name) like lower(concat('%', :text, '%')) " +
                "or lower(i.description) like lower(concat('%', :text, '%'))", Item.class);
        List<Item> result = query
                .setParameter("text", text)
                .getResultList();

        List<Item> items = itemService.searchItem(text, pageable);

        assertThat(result.size()).isEqualTo(items.size());
    }

    @Test
    void save() {
        ItemDto dto = makeItem(item);
        TypedQuery<Item> query = em.createQuery("select i from Item as i where i.owner.email = :email", Item.class);
        item = query
                .setParameter("email", owner.getEmail())
                .getSingleResult();

        assertThat(item.getId()).isNotNull();
        assertThat(item.getName()).isEqualTo(dto.getName());
        assertThat(item.getDescription()).isEqualTo(dto.getDescription());
        assertThat(item.getAvailable()).isEqualTo(dto.getAvailable());
        assertThat(item.getOwner()).isNotNull();
        assertThat(item.getRequest()).isNotNull();
    }

    @Test
    void update() {
        ItemDto dto = makeItem(item);
        TypedQuery<Item> query = em.createQuery("select i from Item as i where i.id = :id", Item.class);
        item = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        Item newItem = Item.builder()
                .id(item.getId())
                .name("Saw")
                .description("Saw 2000")
                .available(true)
                .owner(item.getOwner())
                .request(item.getRequest())
                .build();
        dto = ItemMapper.mapToItemDto(newItem, request);
        dto = itemService.updateItem(owner.getId(), dto.getId(), dto);

        assertThat(newItem.getId()).isNotNull();
        assertThat(newItem.getName()).isEqualTo(dto.getName());
        assertThat(newItem.getDescription()).isEqualTo(dto.getDescription());
        assertThat(newItem.getAvailable()).isEqualTo(dto.getAvailable());
        assertThat(newItem.getOwner()).isNotNull();
        assertThat(newItem.getRequest()).isNotNull();
    }

    @Test
    void deleteById() {
        ItemDto dto = makeItem(item);
        TypedQuery<Long> query = em.createQuery("select i.id from Item as i where i.id = :id", Long.class);
        Long id = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        itemService.deleteItem(owner.getId(), dto.getId());
        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class, () -> {
            itemService.deleteItem(owner.getId(), id);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Вещь с id=%d не найдена!", id);

        assertEquals(expectedMessage, actualMessage);
    }

    private ItemDto makeItem(Item item) {
        UserDto userDto = UserMapper.mapToUserDto(requestor);
        userDto = userService.saveUser(userDto);

        ItemRequestDto requestDto = ItemRequestMapper.mapToItemRequestDto(request);
        requestDto = requestService.addNewRequest(userDto.getId(), requestDto);
        request.setId(requestDto.getId());

        userDto = UserMapper.mapToUserDto(owner);
        userDto = userService.saveUser(userDto);
        owner.setId(userDto.getId());

        ItemDto itemDto = ItemMapper.mapToItemDto(item, request);
        return itemService.addNewItem(userDto.getId(), itemDto);
    }
}
