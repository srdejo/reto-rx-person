package com.pragma.person.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnrollRequestDto {
    @NotNull
    private Long bootcampId;
}
