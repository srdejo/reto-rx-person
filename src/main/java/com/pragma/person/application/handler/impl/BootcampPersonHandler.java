package com.pragma.person.application.handler.impl;

import com.pragma.person.application.dto.request.EnrollRequestDto;
import com.pragma.person.application.dto.response.EnrollResponseDto;
import com.pragma.person.application.handler.IBootcampPersonHandler;
import com.pragma.person.domain.api.IBootcampPersonServicePort;
import com.pragma.person.domain.model.BootcampPersonModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BootcampPersonHandler implements IBootcampPersonHandler {

    private final IBootcampPersonServicePort bootcampPersonServicePort;

    @Override
    public Mono<EnrollResponseDto> enroll(Long personId, EnrollRequestDto enrollRequestDto) {
        return bootcampPersonServicePort.enroll(personId, enrollRequestDto.getBootcampId())
                .map(this::toResponse);
    }

    @Override
    public Flux<EnrollResponseDto> getEnrollments(Long personId) {
        return bootcampPersonServicePort.getEnrollments(personId)
                .map(this::toResponse);
    }

    private EnrollResponseDto toResponse(BootcampPersonModel model) {
        return new EnrollResponseDto(
                model.getId(),
                model.getPersonId(),
                model.getBootcampId(),
                model.getEnrolledAt(),
                model.getBootcampStartDate(),
                model.getBootcampDurationDays());
    }
}
