package com.pragma.person.application.handler;

import com.pragma.person.application.dto.request.LoginRequestDto;
import com.pragma.person.application.dto.request.RegisterRequestDto;
import com.pragma.person.application.dto.response.LoginResponseDto;
import reactor.core.publisher.Mono;

public interface IAuthHandler {

    Mono<LoginResponseDto> login(LoginRequestDto loginRequestDto);

    Mono<LoginResponseDto> register(RegisterRequestDto registerRequestDto);
}
