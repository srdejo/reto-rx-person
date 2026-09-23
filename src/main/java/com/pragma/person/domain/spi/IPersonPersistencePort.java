package com.pragma.person.domain.spi;

import com.pragma.person.domain.model.PersonModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IPersonPersistencePort {
    Mono<PersonModel> savePerson(PersonModel personModel);

    Flux<PersonModel> getAllPersons();

    Mono<PersonModel> findByEmail(String email);
}
