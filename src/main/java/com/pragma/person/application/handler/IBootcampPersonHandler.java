package com.pragma.person.application.handler;

import com.pragma.person.application.dto.request.EnrollRequestDto;
import com.pragma.person.application.dto.response.EnrollResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IBootcampPersonHandler {

    Mono<EnrollResponseDto> enroll(Long personId, EnrollRequestDto enrollRequestDto);

    Flux<EnrollResponseDto> getEnrollments(Long personId);
}
