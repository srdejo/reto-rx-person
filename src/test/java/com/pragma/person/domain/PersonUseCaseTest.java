package com.pragma.person.domain;

import com.pragma.person.domain.model.PersonModel;
import com.pragma.person.domain.spi.IPersonPersistencePort;
import com.pragma.person.domain.usecase.PersonUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class PersonUseCaseTest {

    private final IPersonPersistencePort port = Mockito.mock(IPersonPersistencePort.class);
    private final PersonUseCase useCase = new PersonUseCase(port);

    @Test
    void savePersonDelegatesToPort() {
        PersonModel model = new PersonModel(null, "test");
        Mockito.when(port.savePerson(model)).thenReturn(Mono.just(model));

        StepVerifier.create(useCase.savePerson(model)).expectNext(model).verifyComplete();
    }

    @Test
    void getAllPersonsReturnsFlux() {
        PersonModel model = new PersonModel(null, "test");
        Mockito.when(port.getAllPersons()).thenReturn(Flux.just(model));

        StepVerifier.create(useCase.getAllPersons()).expectNext(model).verifyComplete();
    }
}
