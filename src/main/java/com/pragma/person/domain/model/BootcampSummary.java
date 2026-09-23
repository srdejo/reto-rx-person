package com.pragma.person.domain.model;

import java.time.LocalDate;
import java.util.List;

public record BootcampSummary(Long id, String name, String description, LocalDate startDate, Integer durationDays,
                              List<CapacitySummary> capacities) {
}
