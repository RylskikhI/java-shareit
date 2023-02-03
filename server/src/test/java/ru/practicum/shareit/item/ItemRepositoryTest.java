package ru.practicum.shareit.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class ItemRepositoryTest {
    private User owner;
    private User requestor;
    private ItemRequest request;
    private Item item;
    @Autowired
    private TestEntityManager em;
    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void init() {
        owner = new User(null, "John", "john@gmail.com");
        requestor = new User(null, "Fred", "fred@gmail.com");
        request = new ItemRequest(null, "Circular saw", LocalDateTime.now(), requestor);
        item = Item.builder()
                .id(null)
                .name("Saw")
                .description("Circular saw")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        em.persist(owner);
        em.persist(requestor);
        em.persist(request);
        em.persist(item);
    }

    @AfterEach
    void tearDown() {
        owner = em.find(User.class, owner.getId());
        em.remove(owner);
        requestor = em.find(User.class, requestor.getId());
        em.remove(requestor);
        request = em.find(ItemRequest.class, request.getId());
        em.remove(request);
        item = em.find(Item.class, item.getId());
        em.remove(item);
        em.clear();
    }

    @Test
    void findAllByOwnerId() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Item> items = itemRepository.findAllByOwnerId(owner.getId(), pageable);

        assertNotNull(owner.getId());
        assertNotNull(requestor.getId());
        assertNotNull(request.getId());
        assertNotNull(item.getId());
        assertEquals(1, items.size());
    }

    @Test
    void findByRequestId() {
        Set<Item> items = itemRepository.findByRequestId(request.getId());

        assertNotNull(owner.getId());
        assertNotNull(requestor.getId());
        assertNotNull(request.getId());
        assertNotNull(item.getId());
        assertEquals(1, items.size());
    }

    @Test
    void search() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> items = itemRepository.search("Saw", pageable);

        assertNotNull(owner.getId());
        assertNotNull(requestor.getId());
        assertNotNull(request.getId());
        assertNotNull(item.getId());
        assertEquals(1, items.getTotalElements());
    }
}
