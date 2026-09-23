package com.pragma.person.domain;

import com.pragma.person.domain.exception.BootcampOverlapException;
import com.pragma.person.domain.exception.MaxBootcampsReachedException;
import com.pragma.person.domain.model.BootcampPersonModel;
import com.pragma.person.domain.model.BootcampSummary;
import com.pragma.person.domain.model.CapacitySummary;
import com.pragma.person.domain.model.PersonModel;
import com.pragma.person.domain.model.Role;
import com.pragma.person.domain.model.TechnologySummary;
import com.pragma.person.domain.spi.IBootcampClientPort;
import com.pragma.person.domain.spi.IBootcampPersonPersistencePort;
import com.pragma.person.domain.spi.IPersonPersistencePort;
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
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BootcampPersonUseCaseTest {

    private final IBootcampPersonPersistencePort persistencePort = mock(IBootcampPersonPersistencePort.class);
    private final IPersonPersistencePort personPersistencePort = mock(IPersonPersistencePort.class);
    private final IBootcampClientPort bootcampClientPort = mock(IBootcampClientPort.class);
    private final IReportClientPort reportClientPort = mock(IReportClientPort.class);
    private final BootcampPersonUseCase useCase =
            new BootcampPersonUseCase(persistencePort, personPersistencePort, bootcampClientPort, reportClientPort);

    @Test
    void enrollSuccessfully() {
        Long personId = 1L;
        Long bootcampId = 10L;
        BootcampSummary summary = summary(bootcampId, LocalDate.of(2026, 1, 1), 30);
        BootcampPersonModel saved = givenSuccessfulEnrollment(personId, summary);
        List<PersonModel> persons = givenEnrolledPersons(bootcampId, person(personId, "Ana", "ana@mail.com"));
        when(reportClientPort.sendBootcampReport(ArgumentMatchers.eq(summary), ArgumentMatchers.eq(persons),
                ArgumentMatchers.notNull())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.enroll(personId, bootcampId))
                .expectNext(saved)
                .verifyComplete();
    }

    @Test
    void enrollSendsBootcampReportWithEnrolledPersons() {
        Long personId = 1L;
        Long bootcampId = 10L;
        BootcampSummary summary = summary(bootcampId, LocalDate.of(2026, 1, 1), 30);
        givenSuccessfulEnrollment(personId, summary);
        List<PersonModel> persons = givenEnrolledPersons(bootcampId, person(1L, "Ana", "ana@mail.com"),
                person(2L, "Luis", "luis@mail.com"), person(3L, "Eva", "eva@mail.com"));
        when(reportClientPort.sendBootcampReport(ArgumentMatchers.eq(summary), ArgumentMatchers.eq(persons),
                ArgumentMatchers.notNull())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.enroll(personId, bootcampId))
                .expectNextCount(1)
                .verifyComplete();

        verify(reportClientPort, timeout(1000)).sendBootcampReport(ArgumentMatchers.eq(summary), ArgumentMatchers.eq(persons),
                ArgumentMatchers.notNull());
    }

    @Test
    void enrollCompletesWhenReportFails() {
        Long personId = 1L;
        Long bootcampId = 10L;
        BootcampSummary summary = summary(bootcampId, LocalDate.of(2026, 1, 1), 30);
        BootcampPersonModel saved = givenSuccessfulEnrollment(personId, summary);
        List<PersonModel> persons = givenEnrolledPersons(bootcampId, person(personId, "Ana", "ana@mail.com"));
        when(reportClientPort.sendBootcampReport(ArgumentMatchers.eq(summary), ArgumentMatchers.eq(persons),
                ArgumentMatchers.notNull()))
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
        List<PersonModel> persons = givenEnrolledPersons(bootcampId, person(personId, "Ana", "ana@mail.com"));
        when(reportClientPort.sendBootcampReport(ArgumentMatchers.eq(summary), ArgumentMatchers.eq(persons),
                ArgumentMatchers.notNull())).thenReturn(Mono.never());

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

    private List<PersonModel> givenEnrolledPersons(Long bootcampId, PersonModel... persons) {
        List<BootcampPersonModel> enrollments = Arrays.stream(persons)
                .map(p -> new BootcampPersonModel(p.getId(), p.getId(), bootcampId, LocalDateTime.now(),
                        LocalDate.of(2026, 1, 1), 30))
                .toList();
        List<Long> personIds = Arrays.stream(persons).map(PersonModel::getId).toList();
        when(persistencePort.findByBootcampId(bootcampId)).thenReturn(Flux.fromIterable(enrollments));
        when(personPersistencePort.findAllByIds(personIds)).thenReturn(Flux.just(persons));
        return List.of(persons);
    }

    private PersonModel person(Long id, String name, String email) {
        return new PersonModel(id, name, email, "hashed", LocalDate.of(2000, 1, 1), Role.USER);
    }

    private BootcampPersonModel enrollment(Long bootcampId, LocalDate startDate, int durationDays) {
        return new BootcampPersonModel(bootcampId, 1L, bootcampId, LocalDateTime.now(), startDate, durationDays);
    }
}
