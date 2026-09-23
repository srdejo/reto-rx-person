package com.pragma.person.infrastructure.input.rest;

import com.pragma.person.application.dto.request.EnrollRequestDto;
import com.pragma.person.application.dto.response.EnrollResponseDto;
import com.pragma.person.application.handler.IBootcampPersonHandler;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/bootcamp-person")
@RequiredArgsConstructor
public class BootcampPersonRestController {

    private final IBootcampPersonHandler bootcampPersonHandler;

    @Operation(summary = "Enroll the authenticated person into a bootcamp")
    @PostMapping("/enroll")
    public Mono<ResponseEntity<EnrollResponseDto>> enroll(@Valid @RequestBody EnrollRequestDto enrollRequestDto,
                                                            Mono<Authentication> authenticationMono) {
        return authenticationMono
                .map(authentication -> Long.valueOf((String) Objects.requireNonNull(authentication.getPrincipal())))
                .flatMap(personId -> bootcampPersonHandler.enroll(personId, enrollRequestDto))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Operation(summary = "Get the bootcamps the authenticated person is enrolled in")
    @GetMapping("/me")
    public Flux<EnrollResponseDto> getMyEnrollments(Mono<Authentication> authenticationMono) {
        return authenticationMono
                .map(authentication -> Long.valueOf((String) Objects.requireNonNull(authentication.getPrincipal())))
                .flatMapMany(bootcampPersonHandler::getEnrollments);
    }
}
