package com.pragma.person.domain.spi;

import com.pragma.person.domain.model.BootcampSummary;
import reactor.core.publisher.Mono;

public interface IBootcampClientPort {

    Mono<BootcampSummary> getBootcamp(Long bootcampId);
}
