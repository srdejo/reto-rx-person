package com.pragma.person.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EnrollResponseDto {
    private Long id;
    private Long personId;
    private Long bootcampId;
    private LocalDateTime enrolledAt;
    private LocalDate bootcampStartDate;
    private Integer bootcampDurationDays;
}
