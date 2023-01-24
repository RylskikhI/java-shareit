package ru.practicum.shareit.request.repository;

import java.util.List;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ItemRequestRepositoryTest {
    private User owner;
    private User requestor;
    private ItemRequest request;
    private Item item;
    @Autowired
    private TestEntityManager em;
    @Autowired
    private ItemRequestRepository requestRepository;

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
        em.flush();
        em.clear();
    }

    @Test
    void findAllByRequestorId() {
        List<ItemRequest> requests = requestRepository.findAllByRequestorId(requestor.getId());

        assertNotNull(owner.getId());
        assertNotNull(requestor.getId());
        assertNotNull(request.getId());
        assertNotNull(item.getId());
        assertEquals(1, requests.size());
    }

    @Test
    void findAllByRequestorIdNot() {
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdNot(owner.getId(), Pageable.unpaged());

        assertNotNull(owner.getId());
        assertNotNull(requestor.getId());
        assertNotNull(request.getId());
        assertNotNull(item.getId());
        assertEquals(1, requests.size());
    }
}
