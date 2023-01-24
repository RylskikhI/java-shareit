package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class CommentRepositoryTest {
    private User owner;
    private User author;
    private Item item;
    private Comment comment;
    @Autowired
    private TestEntityManager em;
    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void init() {
        owner = new User(null, "John", "john@gmail.com");
        author = new User(null, "Fred", "fred@gmail.com");
        item = Item.builder()
                .id(null)
                .name("Saw")
                .description("Circular saw")
                .available(true)
                .owner(owner)
                .build();
        comment = new Comment(null, "Perfect saw!", LocalDateTime.now(), item, author);

        em.persist(owner);
        em.persist(author);
        em.persist(item);
        em.persist(comment);
    }

    @AfterEach
    void tearDown() {
        owner = em.find(User.class, owner.getId());
        em.remove(owner);
        author = em.find(User.class, author.getId());
        em.remove(author);
        item = em.find(Item.class, item.getId());
        em.remove(item);
        comment = em.find(Comment.class, comment.getId());
        em.remove(comment);
        em.clear();
    }

    @Test
    void findAllByItemId() {
        Set<Comment> comments = commentRepository.findAllByItemId(item.getId());

        assertNotNull(owner.getId());
        assertNotNull(author.getId());
        assertNotNull(item.getId());
        assertNotNull(comment.getId());
        assertEquals(1, comments.size());
    }
}
