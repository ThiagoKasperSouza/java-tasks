package com.example.taskmanager.exception;

import java.util.Map;

public class BusinessRuleException extends RuntimeException {

    private final Map<String, String> details;

    public BusinessRuleException(String message) {
        this(message, Map.of());
    }

    public BusinessRuleException(String message, Map<String, String> details) {
        super(message);
        this.details = details;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}