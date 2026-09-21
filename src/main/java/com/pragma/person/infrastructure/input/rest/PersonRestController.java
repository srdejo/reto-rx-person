package com.pragma.person.infrastructure.input.rest;

import com.pragma.person.application.dto.request.PersonRequestDto;
import com.pragma.person.application.dto.response.PersonResponseDto;
import com.pragma.person.application.handler.IPersonHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/person")
@RequiredArgsConstructor
public class PersonRestController {

    private final IPersonHandler personHandler;

    @Operation(summary = "Add a new person")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Person created", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
    })
    @PostMapping("/")
    public Mono<ResponseEntity<Void>> savePerson(@Valid @RequestBody PersonRequestDto personRequestDto) {
        return personHandler.savePerson(personRequestDto)
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));
    }

    @Operation(summary = "Get all persons")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All persons returned",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PersonResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "No data found", content = @Content)
    })
    @GetMapping("/")
    public Flux<PersonResponseDto> getAllPersons() {
        return personHandler.getAllPersons();
    }
}
