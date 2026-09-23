package com.pragma.person.domain.usecase;

import com.pragma.person.domain.api.IBootcampPersonServicePort;
import com.pragma.person.domain.exception.BootcampOverlapException;
import com.pragma.person.domain.exception.MaxBootcampsReachedException;
import com.pragma.person.domain.model.BootcampPersonModel;
import com.pragma.person.domain.model.BootcampSummary;
import com.pragma.person.domain.spi.IBootcampClientPort;
import com.pragma.person.domain.spi.IBootcampPersonPersistencePort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class BootcampPersonUseCase implements IBootcampPersonServicePort {

    private static final int MAX_ACTIVE_BOOTCAMPS = 5;

    private final IBootcampPersonPersistencePort bootcampPersonPersistencePort;
    private final IBootcampClientPort bootcampClientPort;

    public BootcampPersonUseCase(IBootcampPersonPersistencePort bootcampPersonPersistencePort,
                                  IBootcampClientPort bootcampClientPort) {
        this.bootcampPersonPersistencePort = bootcampPersonPersistencePort;
        this.bootcampClientPort = bootcampClientPort;
    }

    @Override
    public Mono<BootcampPersonModel> enroll(Long personId, Long bootcampId) {
        return bootcampClientPort.getBootcamp(bootcampId)
                .flatMap(summary -> bootcampPersonPersistencePort.findActiveByPersonId(personId)
                        .collectList()
                        .flatMap(activeEnrollments -> validateAndSave(personId, bootcampId, summary, activeEnrollments)));
    }

    @Override
    public Flux<BootcampPersonModel> getEnrollments(Long personId) {
        return bootcampPersonPersistencePort.findByPersonId(personId);
    }

    private Mono<BootcampPersonModel> validateAndSave(Long personId, Long bootcampId, BootcampSummary summary,
                                                        List<BootcampPersonModel> activeEnrollments) {
        if (activeEnrollments.size() >= MAX_ACTIVE_BOOTCAMPS) {
            return Mono.error(new MaxBootcampsReachedException());
        }

        LocalDate newStart = summary.startDate();
        LocalDate newEnd = summary.startDate().plusDays(summary.durationDays());

        boolean overlaps = activeEnrollments.stream()
                .anyMatch(existing -> rangesOverlap(newStart, newEnd, existing.getBootcampStartDate(), existing.getBootcampEndDate()));

        if (overlaps) {
            return Mono.error(new BootcampOverlapException());
        }

        BootcampPersonModel newEnrollment = new BootcampPersonModel(
                null,
                personId,
                bootcampId,
                LocalDateTime.now(ZoneId.of("America/Bogota")),
                summary.startDate(),
                summary.durationDays());

        return bootcampPersonPersistencePort.save(newEnrollment);
    }

    private boolean rangesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
