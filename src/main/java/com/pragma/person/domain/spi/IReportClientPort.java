package com.pragma.person.domain.spi;

import com.pragma.person.domain.model.BootcampSummary;
import reactor.core.publisher.Mono;

public interface IReportClientPort {

    Mono<Void> sendBootcampReport(BootcampSummary bootcamp, long enrolledCount);
}
