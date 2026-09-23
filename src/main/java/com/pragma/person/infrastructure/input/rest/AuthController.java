package com.pragma.person.infrastructure.input.rest;

import com.pragma.person.application.dto.request.LoginRequestDto;
import com.pragma.person.application.dto.request.RegisterRequestDto;
import com.pragma.person.application.dto.response.LoginResponseDto;
import com.pragma.person.application.handler.IAuthHandler;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthHandler authHandler;

    @Operation(summary = "Login and obtain a JWT token")
    @PostMapping("/login")
    public Mono<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return authHandler.login(loginRequestDto);
    }

    @Operation(summary = "Self-register a new person with role USER and obtain a JWT token")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public Mono<LoginResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        return authHandler.register(registerRequestDto);
    }
}
