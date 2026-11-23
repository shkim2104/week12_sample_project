package edu.handong.week12.common;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(String code, String message, List<FieldError> errors, LocalDateTime timestamp){
    public record FieldError(String field, String reason) {
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, List.of(), LocalDateTime.now());
    }

    public static ErrorResponse of(String code, String message, List<FieldError> errors) {
        return new ErrorResponse(code, message, errors, LocalDateTime.now());
    }
}
