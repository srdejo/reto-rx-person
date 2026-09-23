package com.pragma.person.domain.exception;

public class BootcampNotFoundException extends RuntimeException {
    public BootcampNotFoundException() {
        super("Bootcamp not found");
    }
}
