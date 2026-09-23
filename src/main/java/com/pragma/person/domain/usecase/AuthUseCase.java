package com.pragma.person.domain.usecase;

import com.pragma.person.domain.api.IAuthServicePort;
import com.pragma.person.domain.exception.EmailAlreadyRegisteredException;
import com.pragma.person.domain.exception.InvalidCredentialsException;
import com.pragma.person.domain.model.LoginResult;
import com.pragma.person.domain.model.PersonModel;
import com.pragma.person.domain.model.Role;
import com.pragma.person.domain.spi.IPasswordHasherPort;
import com.pragma.person.domain.spi.IPersonPersistencePort;
import com.pragma.person.domain.spi.ITokenIssuerPort;
import reactor.core.publisher.Mono;

public class AuthUseCase implements IAuthServicePort {

    private final IPersonPersistencePort personPersistencePort;
    private final IPasswordHasherPort passwordHasherPort;
    private final ITokenIssuerPort tokenIssuerPort;

    public AuthUseCase(IPersonPersistencePort personPersistencePort,
                        IPasswordHasherPort passwordHasherPort,
                        ITokenIssuerPort tokenIssuerPort) {
        this.personPersistencePort = personPersistencePort;
        this.passwordHasherPort = passwordHasherPort;
        this.tokenIssuerPort = tokenIssuerPort;
    }

    @Override
    public Mono<LoginResult> login(String email, String password) {
        return personPersistencePort.findByEmail(email)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                .flatMap(person -> checkPassword(person, password));
    }

    private Mono<LoginResult> checkPassword(PersonModel person, String rawPassword) {
        if (!passwordHasherPort.matches(rawPassword, person.getPassword())) {
            return Mono.error(new InvalidCredentialsException());
        }
        return Mono.just(buildLoginResult(person));
    }

    @Override
    public Mono<LoginResult> register(PersonModel personModel) {
        return personPersistencePort.findByEmail(personModel.getEmail())
                .flatMap(existing -> Mono.<LoginResult>error(new EmailAlreadyRegisteredException()))
                .switchIfEmpty(Mono.defer(() -> {
                    personModel.setRole(Role.USER);
                    personModel.setPassword(passwordHasherPort.hash(personModel.getPassword()));
                    return personPersistencePort.savePerson(personModel).map(this::buildLoginResult);
                }));
    }

    private LoginResult buildLoginResult(PersonModel person) {
        String token = tokenIssuerPort.issueToken(person.getId(), person.getEmail(), person.getRole());
        return new LoginResult(token, person.getRole(), person.getId());
    }
}
