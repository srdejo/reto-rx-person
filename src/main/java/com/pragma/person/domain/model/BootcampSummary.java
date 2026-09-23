package com.pragma.person.domain.model;

import java.time.LocalDate;

public record BootcampSummary(Long id, LocalDate startDate, Integer durationDays) {
}
