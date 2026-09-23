package com.pragma.person.infrastructure.configuration;

import com.pragma.person.domain.model.PersonModel;
import com.pragma.person.domain.model.Role;
import com.pragma.person.domain.spi.IPersonPersistencePort;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final IPersonPersistencePort personPersistencePort;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        personPersistencePort.findByEmail(adminEmail)
                .switchIfEmpty(Mono.defer(this::createAdmin))
                .subscribe();
    }

    private Mono<PersonModel> createAdmin() {
        PersonModel admin = new PersonModel(
                null,
                "Admin",
                adminEmail,
                passwordEncoder.encode(adminPassword),
                LocalDate.of(1990, 1, 1),
                Role.ADMIN);
        return personPersistencePort.savePerson(admin);
    }
}
