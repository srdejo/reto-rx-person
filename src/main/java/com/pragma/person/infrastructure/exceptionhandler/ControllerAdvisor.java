package com.pragma.person.infrastructure.exceptionhandler;

import com.pragma.person.domain.exception.BootcampNotFoundException;
import com.pragma.person.domain.exception.BootcampOverlapException;
import com.pragma.person.domain.exception.EmailAlreadyRegisteredException;
import com.pragma.person.domain.exception.InvalidCredentialsException;
import com.pragma.person.domain.exception.MaxBootcampsReachedException;
import com.pragma.person.infrastructure.exception.NoDataFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.Collections;
import java.util.Map;

@RestControllerAdvice
public class ControllerAdvisor {

    private static final String MESSAGE = "message";

    @ExceptionHandler(NoDataFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoDataFoundException(
            NoDataFoundException ignoredNoDataFoundException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap(MESSAGE, ExceptionResponse.NO_DATA_FOUND.getMessage()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(WebExchangeBindException ignored) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap(MESSAGE, ExceptionResponse.INVALID_REQUEST.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentialsException(
            InvalidCredentialsException ignored) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Collections.singletonMap(MESSAGE, ExceptionResponse.INVALID_CREDENTIALS.getMessage()));
    }

    @ExceptionHandler(MaxBootcampsReachedException.class)
    public ResponseEntity<Map<String, String>> handleMaxBootcampsReachedException(
            MaxBootcampsReachedException ignored) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Collections.singletonMap(MESSAGE, ExceptionResponse.MAX_BOOTCAMPS_REACHED.getMessage()));
    }

    @ExceptionHandler(BootcampOverlapException.class)
    public ResponseEntity<Map<String, String>> handleBootcampOverlapException(
            BootcampOverlapException ignored) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Collections.singletonMap(MESSAGE, ExceptionResponse.BOOTCAMP_OVERLAP.getMessage()));
    }

    @ExceptionHandler(BootcampNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleBootcampNotFoundException(
            BootcampNotFoundException ignored) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap(MESSAGE, ExceptionResponse.BOOTCAMP_NOT_FOUND.getMessage()));
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyRegisteredException(
            EmailAlreadyRegisteredException ignored) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Collections.singletonMap(MESSAGE, ExceptionResponse.EMAIL_ALREADY_REGISTERED.getMessage()));
    }
}
