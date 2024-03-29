package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<String> handleValidationException(ValidationException e, HttpServletRequest request) {
        log.warn("Ошибка валидации. {} по пути запроса {}", e.getMessage(), request.getServletPath());
        return new ResponseEntity<>(e.getMessage() + " Путь запроса: "
                + request.getServletPath(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException e, HttpServletRequest request) {
        log.warn("{}. Путь запроса {}", e.getMessage(), request.getServletPath());
        return new ResponseEntity<>(e.getMessage() + " Путь запроса: "
                + request.getServletPath(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ResponseEntity<String> handleItemNotFoundException(ItemNotFoundException e, HttpServletRequest request) {
        log.warn("{}. Путь запроса {}", e.getMessage(), request.getServletPath());
        return new ResponseEntity<>(e.getMessage() + " Путь запроса: "
                + request.getServletPath(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<String> handleBookingStatusException(BookingStatusException e, HttpServletRequest request) {
        log.warn("{}. Путь запроса {}", e.getMessage(), request.getServletPath());
        return new ResponseEntity<>(e.getMessage() + " Путь запроса: "
                + request.getServletPath(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ResponseEntity<String> handleBookingNotFoundException(BookingNotFoundException e, HttpServletRequest request) {
        log.warn("{}. Путь запроса {}", e.getMessage(), request.getServletPath());
        return new ResponseEntity<>(e.getMessage() + " Путь запроса: "
                + request.getServletPath(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BookingStateExistsException.class)
    public ResponseEntity<ErrorResponse> handleBookingStateExistsException(BookingStateExistsException e) {
        final ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ResponseEntity<String> handleSecurityException(SecurityException e, HttpServletRequest request) {
        log.warn("{}. Путь запроса {}", e.getMessage(), request.getServletPath());
        return new ResponseEntity<>(e.getMessage() + " Путь запроса: "
                + request.getServletPath(), HttpStatus.FORBIDDEN);
    }
}
