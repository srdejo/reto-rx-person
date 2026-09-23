package com.pragma.person.domain.usecase;

import com.pragma.person.domain.api.IBootcampPersonServicePort;
import com.pragma.person.domain.exception.BootcampOverlapException;
import com.pragma.person.domain.exception.MaxBootcampsReachedException;
import com.pragma.person.domain.model.BootcampPersonModel;
import com.pragma.person.domain.model.BootcampSummary;
import com.pragma.person.domain.spi.IBootcampClientPort;
import com.pragma.person.domain.spi.IBootcampPersonPersistencePort;
import com.pragma.person.domain.spi.IPersonPersistencePort;
import com.pragma.person.domain.spi.IReportClientPort;
import com.pragma.person.domain.util.DomainConstants;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BootcampPersonUseCase implements IBootcampPersonServicePort {

    private final IBootcampPersonPersistencePort bootcampPersonPersistencePort;
    private final IPersonPersistencePort personPersistencePort;
    private final IBootcampClientPort bootcampClientPort;
    private final IReportClientPort reportClientPort;

    public BootcampPersonUseCase(IBootcampPersonPersistencePort bootcampPersonPersistencePort,
                                  IPersonPersistencePort personPersistencePort,
                                  IBootcampClientPort bootcampClientPort,
                                  IReportClientPort reportClientPort) {
        this.bootcampPersonPersistencePort = bootcampPersonPersistencePort;
        this.personPersistencePort = personPersistencePort;
        this.bootcampClientPort = bootcampClientPort;
        this.reportClientPort = reportClientPort;
    }

    @Override
    public Mono<BootcampPersonModel> enroll(Long personId, Long bootcampId) {
        return bootcampClientPort.getBootcamp(bootcampId)
                .flatMap(summary -> bootcampPersonPersistencePort.findActiveByPersonId(personId)
                        .collectList()
                        .flatMap(activeEnrollments -> validateAndSave(personId, bootcampId, summary, activeEnrollments))
                        .flatMap(saved -> Mono.deferContextual(ctx -> {
                            // Fire-and-forget: propagate the context so the report call keeps the auth token
                            publishReport(summary).contextWrite(ctx).subscribe();
                            return Mono.just(saved);
                        })));
    }

    @Override
    public Flux<BootcampPersonModel> getEnrollments(Long personId) {
        return bootcampPersonPersistencePort.findByPersonId(personId);
    }

    private Mono<BootcampPersonModel> validateAndSave(Long personId, Long bootcampId, BootcampSummary summary,
                                                        List<BootcampPersonModel> activeEnrollments) {
        if (activeEnrollments.size() >= DomainConstants.MAX_ACTIVE_BOOTCAMPS) {
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
                LocalDateTime.now(DomainConstants.ZONE_ID),
                summary.startDate(),
                summary.durationDays());

        return bootcampPersonPersistencePort.save(newEnrollment);
    }

    // Sends the full snapshot of enrolled persons so report-api stores a ready-to-read report.
    // snapshotAt is taken before querying: report-api keeps only the newest snapshot, so a
    // concurrent enrollment whose report arrives late cannot overwrite a more complete one.
    private Mono<Void> publishReport(BootcampSummary bootcamp) {
        return Mono.defer(() -> {
            LocalDateTime snapshotAt = LocalDateTime.now(DomainConstants.ZONE_ID);
            return bootcampPersonPersistencePort
                    .findByBootcampId(bootcamp.id())
                    .map(BootcampPersonModel::getPersonId)
                    .distinct()
                    .collectList()
                    .flatMap(personIds -> personPersistencePort.findAllByIds(personIds).collectList())
                    .flatMap(enrolledPersons ->
                            reportClientPort.sendBootcampReport(bootcamp, enrolledPersons, snapshotAt));
        }).onErrorComplete();
    }

    private boolean rangesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
