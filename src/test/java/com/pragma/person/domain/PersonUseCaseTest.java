package com.pragma.person.domain;

import com.pragma.person.domain.model.PersonModel;
import com.pragma.person.domain.model.Role;
import com.pragma.person.domain.spi.IPasswordHasherPort;
import com.pragma.person.domain.spi.IPersonPersistencePort;
import com.pragma.person.domain.usecase.PersonUseCase;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PersonUseCaseTest {

    private final IPersonPersistencePort port = mock(IPersonPersistencePort.class);
    private final IPasswordHasherPort passwordHasherPort = mock(IPasswordHasherPort.class);
    private final PersonUseCase useCase = new PersonUseCase(port, passwordHasherPort);

    @Test
    void savePersonDelegatesToPort() {
        PersonModel model = new PersonModel(null, "test", "test@test.com", "secret",
                LocalDate.of(1990, 1, 1), Role.USER);
        when(passwordHasherPort.hash("secret")).thenReturn("hashed");
        when(port.savePerson(model)).thenReturn(Mono.just(model));

        StepVerifier.create(useCase.savePerson(model)).expectNext(model).verifyComplete();
    }

    @Test
    void getAllPersonsReturnsFlux() {
        PersonModel model = new PersonModel(null, "test", "test@test.com", "secret",
                LocalDate.of(1990, 1, 1), Role.USER);
        when(port.getAllPersons()).thenReturn(Flux.just(model));

        StepVerifier.create(useCase.getAllPersons()).expectNext(model).verifyComplete();
    }
}
