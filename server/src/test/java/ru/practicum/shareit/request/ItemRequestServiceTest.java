package ru.practicum.shareit.request;

import org.mockito.*;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.time.LocalDateTime;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.MyPageRequest;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.RequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {
    private User owner;
    private User requestor;
    private ItemRequest request;
    private Item item;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemRequestRepository requestRepository;
    @InjectMocks
    private RequestServiceImpl requestService;

    @BeforeEach
    void init() {
        owner = new User(1L, "Elza", "elza@gmail.com");
        requestor = new User(2L, "Olga", "olga@mail.ru");
        request = new ItemRequest(1L, "hairdryer", LocalDateTime.now(), requestor);
        item = Item.builder()
                .id(1L)
                .name("hairdryer")
                .description("New hairdryer Dyson")
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
    void getRequestById() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        Mockito.when(itemRepository.findByRequestId(request.getId())).thenReturn(Set.of(item));

        ItemRequestDto dto = requestService.getRequestById(owner.getId(), request.getId());

        assertEquals(dto.getId(), request.getId());
        assertEquals(dto.getDescription(), request.getDescription());
        assertEquals(dto.getCreated(), request.getCreated());
        assertEquals(dto.getItems().size(), 1);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findByRequestId(request.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void getRequestByNotValidUserId(Long userId) {
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            requestService.getRequestById(userId, request.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void getRequestByNotValidId(Long id) {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class, () -> {
            requestService.getRequestById(owner.getId(), id);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Item request with id=%d not found!", id);

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @Test
    void getAllRequestsMadeByOwner() {
        Mockito.when(userRepository.findById(requestor.getId())).thenReturn(Optional.of(requestor));
        Mockito.when(requestRepository.findAllByRequestorId(requestor.getId())).thenReturn(List.of(request));

        List<ItemRequestDto> requests = requestService.getRequestsMadeByOwner(requestor.getId());

        assertEquals(requests.size(), 1);

        Mockito.verify(userRepository, Mockito.times(1)).findById(requestor.getId());
        Mockito.verify(requestRepository, Mockito.times(2)).findAllByRequestorId(requestor.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void getAllRequestsMadeByOwnerByNotValidUserId(Long userId) {
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            requestService.getRequestsMadeByOwner(userId);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void getAllRequestsMadeByOthers() {
        MyPageRequest pageRequest = new MyPageRequest(0, 10, Sort.by(Sort.Direction.DESC, "created"));
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(requestRepository.findAllByRequestorIdNot(owner.getId(), pageRequest)).thenReturn(List.of(request));

        List<ItemRequestDto> requests = requestService.getRequestsMadeByOthers(owner.getId(), 0, 10);

        assertEquals(requests.size(), 1);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(requestRepository, Mockito.times(2)).findAllByRequestorIdNot(owner.getId(), pageRequest);
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void getAllRequestsMadeByOthersByNotValidUserId(Long userId) {
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            requestService.getRequestsMadeByOthers(userId, 0, 10);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void addNewRequest() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(requestRepository.save(Mockito.any())).thenReturn(request);

        ItemRequestDto dto = ItemRequestMapper.mapToItemRequestDto(request);
        ItemRequestDto savedDto = requestService.addNewRequest(owner.getId(), dto);

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(dto)
                    .usingRecursiveComparison()
                    .isEqualTo(savedDto);
        });

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).save(Mockito.any());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void addNewRequestByNotValidUserId(Long userId) {
        ItemRequestDto dto = ItemRequestMapper.mapToItemRequestDto(request);
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            requestService.addNewRequest(userId, dto);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Пользователь с id=%d не найден!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void deleteRequestById() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        requestService.deleteById(owner.getId(), request.getId());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void deleteRequestByNotValidUserId(Long userId) {
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            requestService.deleteById(userId, request.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void deleteRequestByNotValidId(Long id) {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class, () -> {
            requestService.deleteById(owner.getId(), id);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Item request with id=%d not found!", id);

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }
}
