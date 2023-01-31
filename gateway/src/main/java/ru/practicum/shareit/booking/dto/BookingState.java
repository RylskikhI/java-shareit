package ru.practicum.shareit.booking.dto;

import java.util.Optional;

public enum BookingState {
	ALL,
	CURRENT,
	PAST,
	FUTURE,
	WAITING,
	REJECTED;

	public static Optional<BookingState> from(String state) {
		for (BookingState value : values()) {
			if (value.name().equalsIgnoreCase(state)) {
				return Optional.of(value);
			}
		}
		return Optional.empty();
	}
}
