package com.pragma.person.domain.exception;

public class MaxBootcampsReachedException extends RuntimeException {
    public MaxBootcampsReachedException() {
        super("Person already has the maximum number of active bootcamps");
    }
}
