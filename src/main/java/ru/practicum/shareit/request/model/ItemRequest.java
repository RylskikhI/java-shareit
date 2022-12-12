package ru.practicum.shareit.request.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ItemRequest {
    private long requestId;
    private String requestDescription;
    private long requestorId;
    LocalDate created;
}
