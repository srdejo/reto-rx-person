package com.pragma.person.domain.api;

import com.pragma.person.domain.model.PersonModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IPersonServicePort {

    Mono<PersonModel> savePerson(PersonModel personModel);

    Flux<PersonModel> getAllPersons();
}
