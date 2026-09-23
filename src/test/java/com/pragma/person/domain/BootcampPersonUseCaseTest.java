package com.pragma.person.domain;

import com.pragma.person.domain.exception.BootcampOverlapException;
import com.pragma.person.domain.exception.MaxBootcampsReachedException;
import com.pragma.person.domain.model.BootcampPersonModel;
import com.pragma.person.domain.model.BootcampSummary;
import com.pragma.person.domain.model.CapacitySummary;
import com.pragma.person.domain.model.TechnologySummary;
import com.pragma.person.domain.spi.IBootcampClientPort;
import com.pragma.person.domain.spi.IBootcampPersonPersistencePort;
import com.pragma.person.domain.spi.IReportClientPort;
import com.pragma.person.domain.usecase.BootcampPersonUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BootcampPersonUseCaseTest {

    private final IBootcampPersonPersistencePort persistencePort = mock(IBootcampPersonPersistencePort.class);
    private final IBootcampClientPort bootcampClientPort = mock(IBootcampClientPort.class);
    private final IReportClientPort reportClientPort = mock(IReportClientPort.class);
    private final BootcampPersonUseCase useCase =
            new BootcampPersonUseCase(persistencePort, bootcampClientPort, reportClientPort);

    @Test
    void enrollSuccessfully() {
        Long personId = 1L;
        Long bootcampId = 10L;
        BootcampSummary summary = summary(bootcampId, LocalDate.of(2026, 1, 1), 30);
        BootcampPersonModel saved = givenSuccessfulEnrollment(personId, summary);
        when(persistencePort.countByBootcampId(bootcampId)).thenReturn(Mono.just(1L));
        when(reportClientPort.sendBootcampReport(summary, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.enroll(personId, bootcampId))
                .expectNext(saved)
                .verifyComplete();
    }

    @Test
    void enrollSendsBootcampReportWithEnrolledCount() {
        Long personId = 1L;
        Long bootcampId = 10L;
        BootcampSummary summary = summary(bootcampId, LocalDate.of(2026, 1, 1), 30);
        givenSuccessfulEnrollment(personId, summary);
        when(persistencePort.countByBootcampId(bootcampId)).thenReturn(Mono.just(3L));
        when(reportClientPort.sendBootcampReport(summary, 3L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.enroll(personId, bootcampId))
                .expectNextCount(1)
                .verifyComplete();

        verify(reportClientPort, timeout(1000)).sendBootcampReport(summary, 3L);
    }

    @Test
    void enrollCompletesWhenReportFails() {
        Long personId = 1L;
        Long bootcampId = 10L;
        BootcampSummary summary = summary(bootcampId, LocalDate.of(2026, 1, 1), 30);
        BootcampPersonModel saved = givenSuccessfulEnrollment(personId, summary);
        when(persistencePort.countByBootcampId(bootcampId)).thenReturn(Mono.just(1L));
        when(reportClientPort.sendBootcampReport(summary, 1L))
                .thenReturn(Mono.error(new IllegalStateException("report-api down")));

        StepVerifier.create(useCase.enroll(personId, bootcampId))
                .expectNext(saved)
                .verifyComplete();
    }

    @Test
    void enrollDoesNotWaitForReport() {
        Long personId = 1L;
        Long bootcampId = 10L;
        BootcampSummary summary = summary(bootcampId, LocalDate.of(2026, 1, 1), 30);
        BootcampPersonModel saved = givenSuccessfulEnrollment(personId, summary);
        when(persistencePort.countByBootcampId(bootcampId)).thenReturn(Mono.just(1L));
        when(reportClientPort.sendBootcampReport(summary, 1L)).thenReturn(Mono.never());

        StepVerifier.create(useCase.enroll(personId, bootcampId))
                .expectNext(saved)
                .expectComplete()
                .verify(Duration.ofSeconds(1));
    }

    @Test
    void rejectsWhenMaxActiveBootcampsReached() {
        Long personId = 1L;
        Long bootcampId = 10L;
        BootcampSummary summary = summary(bootcampId, LocalDate.of(2026, 1, 1), 30);

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

        verifyNoInteractions(reportClientPort);
    }

    @Test
    void rejectsOverlappingDates() {
        Long personId = 1L;
        Long bootcampId = 10L;
        BootcampSummary summary = summary(bootcampId, LocalDate.of(2026, 1, 10), 30);

        when(bootcampClientPort.getBootcamp(bootcampId)).thenReturn(Mono.just(summary));

        BootcampPersonModel existing = enrollment(20L, LocalDate.of(2026, 1, 1), 30);
        when(persistencePort.findActiveByPersonId(personId)).thenReturn(Flux.just(existing));

        StepVerifier.create(useCase.enroll(personId, bootcampId))
                .expectError(BootcampOverlapException.class)
                .verify();

        verifyNoInteractions(reportClientPort);
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

    private BootcampPersonModel givenSuccessfulEnrollment(Long personId, BootcampSummary summary) {
        when(bootcampClientPort.getBootcamp(summary.id())).thenReturn(Mono.just(summary));
        when(persistencePort.findActiveByPersonId(personId)).thenReturn(Flux.empty());

        BootcampPersonModel saved = new BootcampPersonModel(1L, personId, summary.id(), LocalDateTime.now(),
                summary.startDate(), summary.durationDays());
        when(persistencePort.save(ArgumentMatchers.any())).thenReturn(Mono.just(saved));
        return saved;
    }

    private BootcampSummary summary(Long bootcampId, LocalDate startDate, int durationDays) {
        List<CapacitySummary> capacities = List.of(
                new CapacitySummary(1L, "Backend", List.of(new TechnologySummary(1L, "Java"))));
        return new BootcampSummary(bootcampId, "Bootcamp", "Description", startDate, durationDays, capacities);
    }

    private BootcampPersonModel enrollment(Long bootcampId, LocalDate startDate, int durationDays) {
        return new BootcampPersonModel(bootcampId, 1L, bootcampId, LocalDateTime.now(), startDate, durationDays);
    }
}
