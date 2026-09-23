package com.pragma.person.domain;

import com.pragma.person.domain.exception.BootcampOverlapException;
import com.pragma.person.domain.exception.MaxBootcampsReachedException;
import com.pragma.person.domain.model.BootcampPersonModel;
import com.pragma.person.domain.model.BootcampSummary;
import com.pragma.person.domain.spi.IBootcampClientPort;
import com.pragma.person.domain.spi.IBootcampPersonPersistencePort;
import com.pragma.person.domain.usecase.BootcampPersonUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BootcampPersonUseCaseTest {

    private final IBootcampPersonPersistencePort persistencePort = mock(IBootcampPersonPersistencePort.class);
    private final IBootcampClientPort bootcampClientPort = mock(IBootcampClientPort.class);
    private final BootcampPersonUseCase useCase = new BootcampPersonUseCase(persistencePort, bootcampClientPort);

    @Test
    void enrollSuccessfully() {
        Long personId = 1L;
        Long bootcampId = 10L;
        BootcampSummary summary = new BootcampSummary(bootcampId, LocalDate.of(2026, 1, 1), 30);

        when(bootcampClientPort.getBootcamp(bootcampId)).thenReturn(Mono.just(summary));
        when(persistencePort.findActiveByPersonId(personId)).thenReturn(Flux.empty());

        BootcampPersonModel saved = new BootcampPersonModel(1L, personId, bootcampId, LocalDateTime.now(),
                summary.startDate(), summary.durationDays());
        when(persistencePort.save(ArgumentMatchers.any())).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.enroll(personId, bootcampId))
                .expectNext(saved)
                .verifyComplete();
    }

    @Test
    void rejectsWhenMaxActiveBootcampsReached() {
        Long personId = 1L;
        Long bootcampId = 10L;
        BootcampSummary summary = new BootcampSummary(bootcampId, LocalDate.of(2026, 1, 1), 30);

        when(bootcampClientPort.getBootcamp(bootcampId)).thenReturn(Mono.just(summary));

        List<BootcampPersonModel> activeEnrollments = List.of(
                enrollment(2L, LocalDate.of(2026, 3, 1), 10),
                enrollment(3L, LocalDate.of(2026, 4, 1), 10),
                enrollment(4L, LocalDate.of(2026, 5, 1), 10),
                enrollment(5L, LocalDate.of(2026, 6, 1), 10),
                enrollment(6L, LocalDate.of(2026, 7, 1), 10));
        when(persistencePort.findActiveByPersonId(personId)).thenReturn(Flux.fromIterable(activeEnrollments));

        StepVerifier.create(useCase.enroll(personId, bootcampId))
                .expectError(MaxBootcampsReachedException.class)
                .verify();
    }

    @Test
    void rejectsOverlappingDates() {
        Long personId = 1L;
        Long bootcampId = 10L;
        BootcampSummary summary = new BootcampSummary(bootcampId, LocalDate.of(2026, 1, 10), 30);

        when(bootcampClientPort.getBootcamp(bootcampId)).thenReturn(Mono.just(summary));

        BootcampPersonModel existing = enrollment(20L, LocalDate.of(2026, 1, 1), 30);
        when(persistencePort.findActiveByPersonId(personId)).thenReturn(Flux.just(existing));

        StepVerifier.create(useCase.enroll(personId, bootcampId))
                .expectError(BootcampOverlapException.class)
                .verify();
    }

    @Test
    void returnsAllEnrollmentsOfPerson() {
        Long personId = 1L;
        BootcampPersonModel first = enrollment(2L, LocalDate.of(2025, 1, 1), 10);
        BootcampPersonModel second = enrollment(3L, LocalDate.of(2026, 3, 1), 10);
        when(persistencePort.findByPersonId(personId)).thenReturn(Flux.just(first, second));

        StepVerifier.create(useCase.getEnrollments(personId))
                .expectNext(first, second)
                .verifyComplete();
    }

    private BootcampPersonModel enrollment(Long bootcampId, LocalDate startDate, int durationDays) {
        return new BootcampPersonModel(bootcampId, 1L, bootcampId, LocalDateTime.now(), startDate, durationDays);
    }
}
