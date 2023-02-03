package ru.practicum.shareit.user.dto;

import lombok.*;
import javax.validation.constraints.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDto {
    private Long id;
    private String name;
    @Email
    private String email;
}
