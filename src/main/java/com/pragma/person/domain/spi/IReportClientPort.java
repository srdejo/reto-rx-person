package com.pragma.person.domain.spi;

import com.pragma.person.domain.model.BootcampSummary;
import com.pragma.person.domain.model.PersonModel;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface IReportClientPort {

    Mono<Void> sendBootcampReport(BootcampSummary bootcamp, List<PersonModel> enrolledPersons, LocalDateTime snapshotAt);
}
