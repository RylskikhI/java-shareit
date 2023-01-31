package ru.practicum.shareit.booking.validator;

import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EndDateShouldBeAfterStartDateValidator implements ConstraintValidator<EndDateShouldBeAfterStartDate, BookingDto> {

    @Override
    public boolean isValid(BookingDto bookingDto, ConstraintValidatorContext context) {
        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            context.buildConstraintViolationWithTemplate("End date must be after start date")
                    .addPropertyNode("end")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
