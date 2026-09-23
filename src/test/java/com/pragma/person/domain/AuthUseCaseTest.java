package com.pragma.person.domain;

import com.pragma.person.domain.exception.EmailAlreadyRegisteredException;
import com.pragma.person.domain.exception.InvalidCredentialsException;
import com.pragma.person.domain.model.PersonModel;
import com.pragma.person.domain.model.Role;
import com.pragma.person.domain.spi.IPasswordHasherPort;
import com.pragma.person.domain.spi.IPersonPersistencePort;
import com.pragma.person.domain.spi.ITokenIssuerPort;
import com.pragma.person.domain.usecase.AuthUseCase;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

class AuthUseCaseTest {

    private final IPersonPersistencePort port = mock(IPersonPersistencePort.class);
    private final IPasswordHasherPort passwordHasherPort = mock(IPasswordHasherPort.class);
    private final ITokenIssuerPort tokenIssuerPort = mock(ITokenIssuerPort.class);
    private final AuthUseCase useCase = new AuthUseCase(port, passwordHasherPort, tokenIssuerPort);

    @Test
    void registerSucceedsWhenEmailIsNew() {
        PersonModel input = new PersonModel(null, "test", "test@test.com", "secret",
                LocalDate.of(1990, 1, 1), null);
        PersonModel saved = new PersonModel(1L, "test", "test@test.com", "hashed",
                LocalDate.of(1990, 1, 1), Role.USER);

        when(port.findByEmail("test@test.com")).thenReturn(Mono.empty());
        when(passwordHasherPort.hash("secret")).thenReturn("hashed");
        when(port.savePerson(any(PersonModel.class))).thenReturn(Mono.just(saved));
        when(tokenIssuerPort.issueToken(1L, "test@test.com", Role.USER)).thenReturn("token");

        StepVerifier.create(useCase.register(input))
                .expectNextMatches(result -> "token".equals(result.getToken())
                        && result.getRole() == Role.USER
                        && result.getPersonId().equals(1L))
                .verifyComplete();
    }

    @Test
    void registerFailsWhenEmailAlreadyExists() {
        PersonModel input = new PersonModel(null, "test", "test@test.com", "secret",
                LocalDate.of(1990, 1, 1), null);
        PersonModel existing = new PersonModel(1L, "test", "test@test.com", "hashed",
                LocalDate.of(1990, 1, 1), Role.USER);

        when(port.findByEmail("test@test.com")).thenReturn(Mono.just(existing));

        StepVerifier.create(useCase.register(input))
                .expectError(EmailAlreadyRegisteredException.class)
                .verify();

        verify(port, never()).savePerson(any());
    }

    @Test
    void loginFailsWhenEmailNotFound() {
        when(port.findByEmail("missing@test.com")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.login("missing@test.com", "secret"))
                .expectError(InvalidCredentialsException.class)
                .verify();
    }
}
