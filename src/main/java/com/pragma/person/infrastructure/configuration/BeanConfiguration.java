package com.pragma.person.infrastructure.configuration;

import com.pragma.person.domain.api.IPersonServicePort;
import com.pragma.person.domain.spi.IPersonPersistencePort;
import com.pragma.person.domain.usecase.PersonUseCase;
import com.pragma.person.infrastructure.out.r2dbc.adapter.PersonAdapter;
import com.pragma.person.infrastructure.out.r2dbc.mapper.IPersonEntityMapper;
import com.pragma.person.infrastructure.out.r2dbc.repository.IPersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {
    private final IPersonRepository personRepository;
    private final IPersonEntityMapper personEntityMapper;

    @Bean
    public IPersonPersistencePort personPersistencePort() {
        return new PersonAdapter(personRepository, personEntityMapper);
    }

    @Bean
    public IPersonServicePort personServicePort() {
        return new PersonUseCase(personPersistencePort());
    }
}
