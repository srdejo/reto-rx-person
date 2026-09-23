package com.pragma.person.application.dto.response;

import com.pragma.person.domain.model.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PersonResponseDto {
    private Long id;
    private String name;
    private String email;
    private LocalDate birthDate;
    private Role role;
}
