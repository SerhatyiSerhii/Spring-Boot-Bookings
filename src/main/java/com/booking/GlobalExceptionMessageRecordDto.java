package com.booking;

import java.time.LocalDateTime;

public record GlobalExceptionMessageRecordDto(
        String message,
        String detailedMessage,
        LocalDateTime erorDateTime) {
};
