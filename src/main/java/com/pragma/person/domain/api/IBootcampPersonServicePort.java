package com.pragma.person.domain.api;

import com.pragma.person.domain.model.BootcampPersonModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IBootcampPersonServicePort {

    Mono<BootcampPersonModel> enroll(Long personId, Long bootcampId);

    Flux<BootcampPersonModel> getEnrollments(Long personId);
}
