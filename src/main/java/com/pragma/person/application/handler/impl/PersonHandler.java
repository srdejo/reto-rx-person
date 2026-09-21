package com.pragma.person.application.handler.impl;

import com.pragma.person.application.dto.request.PersonRequestDto;
import com.pragma.person.application.dto.response.PersonResponseDto;
import com.pragma.person.application.handler.IPersonHandler;
import com.pragma.person.application.mapper.IPersonRequestMapper;
import com.pragma.person.application.mapper.IPersonResponseMapper;
import com.pragma.person.domain.api.IPersonServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PersonHandler implements IPersonHandler {

    private final IPersonServicePort personServicePort;
    private final IPersonRequestMapper personRequestMapper;
    private final IPersonResponseMapper personResponseMapper;

    @Override
    public Mono<Void> savePerson(PersonRequestDto personRequestDto) {
        return personServicePort.savePerson(personRequestMapper.toPerson(personRequestDto)).then();
    }

    @Override
    public Flux<PersonResponseDto> getAllPersons() {
        return personServicePort.getAllPersons().map(personResponseMapper::toResponse);
    }
}
