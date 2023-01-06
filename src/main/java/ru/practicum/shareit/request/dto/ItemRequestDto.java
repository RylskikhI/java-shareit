package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ItemRequestDto {
    private long requestId;
    private String requestDescription;
    private long requestorId;
    LocalDate created;
}
