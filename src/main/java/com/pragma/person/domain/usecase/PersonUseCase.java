package com.pragma.person.domain.usecase;

import com.pragma.person.domain.api.IPersonServicePort;
import com.pragma.person.domain.model.PersonModel;
import com.pragma.person.domain.model.Role;
import com.pragma.person.domain.spi.IPasswordHasherPort;
import com.pragma.person.domain.spi.IPersonPersistencePort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PersonUseCase implements IPersonServicePort {

    private final IPersonPersistencePort personPersistencePort;
    private final IPasswordHasherPort passwordHasherPort;

    public PersonUseCase(IPersonPersistencePort personPersistencePort, IPasswordHasherPort passwordHasherPort) {
        this.personPersistencePort = personPersistencePort;
        this.passwordHasherPort = passwordHasherPort;
    }

    @Override
    public Mono<PersonModel> savePerson(PersonModel personModel) {
        if (personModel.getRole() == null) {
            personModel.setRole(Role.USER);
        }
        if (personModel.getPassword() != null) {
            personModel.setPassword(passwordHasherPort.hash(personModel.getPassword()));
        }
        return personPersistencePort.savePerson(personModel);
    }

    @Override
    public Flux<PersonModel> getAllPersons() {
        return personPersistencePort.getAllPersons();
    }
}
