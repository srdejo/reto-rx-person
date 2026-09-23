package com.pragma.person.infrastructure.configuration;

import com.pragma.person.domain.api.IAuthServicePort;
import com.pragma.person.domain.api.IBootcampPersonServicePort;
import com.pragma.person.domain.api.IPersonServicePort;
import com.pragma.person.domain.spi.IBootcampClientPort;
import com.pragma.person.domain.spi.IBootcampPersonPersistencePort;
import com.pragma.person.domain.spi.IPasswordHasherPort;
import com.pragma.person.domain.spi.IPersonPersistencePort;
import com.pragma.person.domain.spi.IReportClientPort;
import com.pragma.person.domain.spi.ITokenIssuerPort;
import com.pragma.person.domain.usecase.AuthUseCase;
import com.pragma.person.domain.usecase.BootcampPersonUseCase;
import com.pragma.person.domain.usecase.PersonUseCase;
import com.pragma.person.infrastructure.out.r2dbc.adapter.BootcampPersonAdapter;
import com.pragma.person.infrastructure.out.r2dbc.adapter.PersonAdapter;
import com.pragma.person.infrastructure.out.r2dbc.mapper.IBootcampPersonEntityMapper;
import com.pragma.person.infrastructure.out.r2dbc.mapper.IPersonEntityMapper;
import com.pragma.person.infrastructure.out.r2dbc.repository.IBootcampPersonRepository;
import com.pragma.person.infrastructure.out.r2dbc.repository.IPersonRepository;
import com.pragma.person.infrastructure.out.webclient.adapter.BootcampWebClientAdapter;
import com.pragma.person.infrastructure.out.webclient.adapter.ReportWebClientAdapter;
import com.pragma.person.infrastructure.out.webclient.mapper.IBootcampClientMapper;
import com.pragma.person.infrastructure.out.webclient.mapper.IReportClientMapper;
import com.pragma.person.infrastructure.security.JwtAuthenticationWebFilter;
import com.pragma.person.infrastructure.security.JwtService;
import com.pragma.person.infrastructure.security.adapter.BCryptPasswordHasherAdapter;
import com.pragma.person.infrastructure.security.adapter.JwtTokenIssuerAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {
    private final IPersonRepository personRepository;
    private final IPersonEntityMapper personEntityMapper;
    private final IBootcampPersonRepository bootcampPersonRepository;
    private final IBootcampPersonEntityMapper bootcampPersonEntityMapper;
    private final IBootcampClientMapper bootcampClientMapper;
    private final IReportClientMapper reportClientMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Value("${webclient.bootcamp}")
    private String bootcampUrl;

    @Value("${webclient.report}")
    private String reportUrl;

    @Bean
    public IPersonPersistencePort personPersistencePort() {
        return new PersonAdapter(personRepository, personEntityMapper);
    }

    @Bean
    public IPasswordHasherPort passwordHasherPort() {
        return new BCryptPasswordHasherAdapter(passwordEncoder);
    }

    @Bean
    public IPersonServicePort personServicePort() {
        return new PersonUseCase(personPersistencePort(), passwordHasherPort());
    }

    @Bean
    public JwtService jwtService() {
        return new JwtService(jwtSecret, jwtExpirationMs);
    }

    @Bean
    public JwtAuthenticationWebFilter jwtAuthenticationWebFilter() {
        return new JwtAuthenticationWebFilter(jwtService());
    }

    @Bean
    public ITokenIssuerPort tokenIssuerPort() {
        return new JwtTokenIssuerAdapter(jwtService());
    }

    @Bean
    public IAuthServicePort authServicePort() {
        return new AuthUseCase(personPersistencePort(), passwordHasherPort(), tokenIssuerPort());
    }

    @Bean
    public IBootcampPersonPersistencePort bootcampPersonPersistencePort() {
        return new BootcampPersonAdapter(bootcampPersonRepository, bootcampPersonEntityMapper);
    }

    @Bean
    public IBootcampClientPort bootcampClientPort() {
        return new BootcampWebClientAdapter(WebClient.builder(), bootcampUrl, bootcampClientMapper);
    }

    @Bean
    public IReportClientPort reportClientPort() {
        return new ReportWebClientAdapter(WebClient.builder(), reportUrl, reportClientMapper);
    }

    @Bean
    public IBootcampPersonServicePort bootcampPersonServicePort() {
        return new BootcampPersonUseCase(bootcampPersonPersistencePort(), personPersistencePort(),
                bootcampClientPort(), reportClientPort());
    }

    @Bean
    public AdminSeeder adminSeeder() {
        return new AdminSeeder(personPersistencePort(), passwordEncoder);
    }
}
