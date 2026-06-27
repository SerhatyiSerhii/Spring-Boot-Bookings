package com.booking;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.persistence.EntityNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalExceptionMessageRecordDto> handleGenericException(Exception e) {
        log.error("Handle Exception", e);

        var errorMessage = new GlobalExceptionMessageRecordDto("Internal server error", e.getMessage(),
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<GlobalExceptionMessageRecordDto> handleEntityNotFoundException(EntityNotFoundException e) {
        log.error("Handle EntityNotFoundException", e);

        var errorMessage = new GlobalExceptionMessageRecordDto("Entity not found", e.getMessage(), LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<GlobalExceptionMessageRecordDto> handleBadReques(RuntimeException e) {
        log.error("Handle BadRequest", e);

        var errorMessage = new GlobalExceptionMessageRecordDto("Bad request", e.getMessage(), LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

}
