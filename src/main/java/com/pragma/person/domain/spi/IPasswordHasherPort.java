package com.pragma.person.domain.spi;

public interface IPasswordHasherPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);
}
