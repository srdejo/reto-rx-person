package com.pragma.person.domain.model;

import java.util.List;

public record CapacitySummary(Long id, String name, List<TechnologySummary> technologies) {
}
