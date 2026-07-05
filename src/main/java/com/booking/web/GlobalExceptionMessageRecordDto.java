package com.booking.web;

import java.time.LocalDateTime;

public record GlobalExceptionMessageRecordDto(
        String message,
        String detailedMessage,
        LocalDateTime erorDateTime) {
};
