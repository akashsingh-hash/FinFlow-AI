package com.finflow.ai.exception;

import lombok.Getter;
import java.util.List;

@Getter
public class ValidationException extends BusinessException {
    private final List<String> errors;

    public ValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }
}
