package com.pragma.person.infrastructure.exceptionhandler;

import lombok.Getter;

@Getter
public enum ExceptionResponse {
    NO_DATA_FOUND("No data found for the requested petition"),
    INVALID_REQUEST("Invalid request body"),
    INVALID_CREDENTIALS("Invalid email or password"),
    MAX_BOOTCAMPS_REACHED("Person already has the maximum number of active bootcamps"),
    BOOTCAMP_OVERLAP("The bootcamp dates overlap with an existing active enrollment"),
    BOOTCAMP_NOT_FOUND("Bootcamp not found"),
    EMAIL_ALREADY_REGISTERED("Email is already registered");

    private final String message;

    ExceptionResponse(String message) {
        this.message = message;
    }

}
