package com.pragma.person.domain.spi;

import com.pragma.person.domain.model.BootcampPersonModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IBootcampPersonPersistencePort {

    Mono<BootcampPersonModel> save(BootcampPersonModel bootcampPersonModel);

    Flux<BootcampPersonModel> findActiveByPersonId(Long personId);

    Flux<BootcampPersonModel> findByPersonId(Long personId);

    Mono<Long> countByBootcampId(Long bootcampId);
}
