package com.pragma.person.domain.spi;

import com.pragma.person.domain.model.Role;

public interface ITokenIssuerPort {

    String issueToken(Long personId, String email, Role role);
}
