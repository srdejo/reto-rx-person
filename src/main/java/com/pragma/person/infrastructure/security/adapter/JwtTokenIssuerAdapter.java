package com.pragma.person.infrastructure.security.adapter;

import com.pragma.person.domain.model.Role;
import com.pragma.person.domain.spi.ITokenIssuerPort;
import com.pragma.person.infrastructure.security.JwtService;

public class JwtTokenIssuerAdapter implements ITokenIssuerPort {

    private final JwtService jwtService;

    public JwtTokenIssuerAdapter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public String issueToken(Long personId, String email, Role role) {
        return jwtService.generateToken(String.valueOf(personId), email, role.name());
    }
}
