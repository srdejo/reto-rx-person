package com.pragma.person.application.handler.impl;

import com.pragma.person.application.dto.request.LoginRequestDto;
import com.pragma.person.application.dto.request.RegisterRequestDto;
import com.pragma.person.application.dto.response.LoginResponseDto;
import com.pragma.person.application.handler.IAuthHandler;
import com.pragma.person.application.mapper.IPersonRequestMapper;
import com.pragma.person.domain.api.IAuthServicePort;
import com.pragma.person.domain.model.LoginResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthHandler implements IAuthHandler {

    private final IAuthServicePort authServicePort;
    private final IPersonRequestMapper personRequestMapper;

    @Override
    public Mono<LoginResponseDto> login(LoginRequestDto loginRequestDto) {
        return authServicePort.login(loginRequestDto.getEmail(), loginRequestDto.getPassword())
                .map(this::toResponse);
    }

    @Override
    public Mono<LoginResponseDto> register(RegisterRequestDto registerRequestDto) {
        return authServicePort.register(personRequestMapper.toPerson(registerRequestDto))
                .map(this::toResponse);
    }

    private LoginResponseDto toResponse(LoginResult result) {
        return new LoginResponseDto(result.getToken(), result.getRole(), result.getPersonId());
    }
}
