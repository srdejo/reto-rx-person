package com.pragma.person.application.handler;

import com.pragma.person.application.dto.request.PersonRequestDto;
import com.pragma.person.application.dto.response.PersonResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IPersonHandler {

    Mono<Void> savePerson(PersonRequestDto personRequestDto);

    Flux<PersonResponseDto> getAllPersons();
}
