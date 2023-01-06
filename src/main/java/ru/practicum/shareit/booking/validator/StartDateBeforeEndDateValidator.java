package ru.practicum.shareit.booking.validator;

import ru.practicum.shareit.booking.model.Booking;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class StartDateBeforeEndDateValidator implements ConstraintValidator<StartDateBeforeEndDate, Booking> {
    @Override
    public boolean isValid(Booking booking, ConstraintValidatorContext context) {
        if (booking.getStart() == null || booking.getEnd() == null) {
            return true;
        }
        return booking.getStart().isBefore(booking.getEnd());
    }
}
