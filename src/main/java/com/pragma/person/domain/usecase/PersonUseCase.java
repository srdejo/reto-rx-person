package com.pragma.person.domain.usecase;

import com.pragma.person.domain.api.IPersonServicePort;
import com.pragma.person.domain.model.PersonModel;
import com.pragma.person.domain.spi.IPersonPersistencePort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PersonUseCase implements IPersonServicePort {

    private final IPersonPersistencePort personPersistencePort;

    public PersonUseCase(IPersonPersistencePort personPersistencePort) {
        this.personPersistencePort = personPersistencePort;
    }

    @Override
    public Mono<PersonModel> savePerson(PersonModel personModel) {
        return personPersistencePort.savePerson(personModel);
    }

    @Override
    public Flux<PersonModel> getAllPersons() {
        return personPersistencePort.getAllPersons();
    }
}
