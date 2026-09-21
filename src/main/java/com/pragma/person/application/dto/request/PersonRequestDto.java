package com.pragma.person.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonRequestDto {
    @NotBlank
    @Size(max = 50)
    private String name;
}
