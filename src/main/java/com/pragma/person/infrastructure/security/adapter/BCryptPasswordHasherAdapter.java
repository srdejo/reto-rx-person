package com.pragma.person.infrastructure.security.adapter;

import com.pragma.person.domain.spi.IPasswordHasherPort;
import org.springframework.security.crypto.password.PasswordEncoder;

public class BCryptPasswordHasherAdapter implements IPasswordHasherPort {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordHasherAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}
