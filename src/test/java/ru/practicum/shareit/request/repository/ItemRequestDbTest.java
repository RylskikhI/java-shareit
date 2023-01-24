package ru.practicum.shareit.request.repository;

import java.util.List;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.TypedQuery;
import javax.persistence.EntityManager;

import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestDbTest {
    private User requestor;
    private ItemRequest request;
    private final EntityManager em;
    private final UserService userService;
    private final RequestService requestService;

    @BeforeEach
    void init() {
        requestor = new User(null, "Eric", "eric@yahoo.com");
        request = new ItemRequest(null, "Want to rent circular saw!", LocalDateTime.now(), requestor);
    }

    @AfterEach
    void tearDown() {
        requestor = null;
        request = null;
    }

    @Test
    void getRequestById() {
        ItemRequestDto dto = makeItemRequest(request);
        TypedQuery<Long> query = em.createQuery("select r.id from ItemRequest as r where r.id = :id", Long.class);
        Long id = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        dto = requestService.getRequestById(requestor.getId(), id);

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getDescription()).isEqualTo(request.getDescription());
        assertThat(dto.getCreated()).isBefore(LocalDateTime.now());
        assertThat(dto.getItems().size()).isEqualTo(0);
    }

    @Test
    void getAllRequestsMadeByOwner() {
        makeItemRequests();
        TypedQuery<ItemRequest> query = em.createQuery("select r from ItemRequest as r where r.requestor.id = :id",
                ItemRequest.class);
        List<ItemRequest> result = query
                .setParameter("id", requestor.getId())
                .getResultList();

        List<ItemRequestDto> requests = requestService.getRequestsMadeByOwner(requestor.getId());

        assertThat(result.size()).isEqualTo(requests.size());
    }

    @Test
    void getAllRequestsMadeByOthers() {
        makeItemRequests();
        TypedQuery<ItemRequest> query = em.createQuery("select r from ItemRequest as r where not r.requestor.id = :id" +
                " order by r.created desc", ItemRequest.class);
        List<ItemRequest> result = query
                .setParameter("id", requestor.getId())
                .getResultList();

        List<ItemRequestDto> requests = requestService.getRequestsMadeByOthers(requestor.getId(), 0, 10);

        assertThat(result.size()).isEqualTo(requests.size());
    }

    @Test
    void addNewItemRequest() {
        ItemRequestDto dto = makeItemRequest(request);
        TypedQuery<ItemRequest> query = em.createQuery("select r from ItemRequest as r where r.requestor.email = :email", ItemRequest.class);
        request = query
                .setParameter("email", requestor.getEmail())
                .getSingleResult();

        assertThat(request.getId()).isNotNull();
        assertThat(request.getDescription()).isEqualTo(dto.getDescription());
        assertThat(request.getCreated()).isBefore(LocalDateTime.now());
        assertThat(request.getRequestor().getId()).isNotNull();
    }

    @Test
    void deleteRequestById() {
        ItemRequestDto dto = makeItemRequest(request);
        TypedQuery<Long> query = em.createQuery("select r.id from ItemRequest as r where r.id = :id", Long.class);
        Long id = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        requestService.deleteById(requestor.getId(), id);
        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class, () -> {
            requestService.getRequestById(requestor.getId(), id);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Item request with id=%d not found!", id);

        assertEquals(expectedMessage, actualMessage);
    }

    private ItemRequestDto makeItemRequest(ItemRequest request) {
        UserDto userDto = UserMapper.mapToUserDto(requestor);
        userDto = userService.saveUser(userDto);
        requestor.setId(userDto.getId());

        ItemRequestDto dto = ItemRequestMapper.mapToItemRequestDto(request);
        return requestService.addNewRequest(userDto.getId(), dto);
    }

    private void makeItemRequests() {
        UserDto userDto = UserMapper.mapToUserDto(requestor);
        userDto = userService.saveUser(userDto);
        requestor.setId(userDto.getId());

        ItemRequestDto dto = ItemRequestMapper.mapToItemRequestDto(request);
        requestService.addNewRequest(userDto.getId(), dto);
        requestService.addNewRequest(userDto.getId(), dto);
        requestService.addNewRequest(userDto.getId(), dto);
    }
}
