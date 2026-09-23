package com.pragma.person.domain.exception;

public class BootcampOverlapException extends RuntimeException {
    public BootcampOverlapException() {
        super("The bootcamp dates overlap with an existing active enrollment");
    }
}
