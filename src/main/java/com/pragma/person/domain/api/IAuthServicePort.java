package com.pragma.person.domain.api;

import com.pragma.person.domain.model.LoginResult;
import com.pragma.person.domain.model.PersonModel;
import reactor.core.publisher.Mono;

public interface IAuthServicePort {

    Mono<LoginResult> login(String email, String password);

    Mono<LoginResult> register(PersonModel personModel);
}
