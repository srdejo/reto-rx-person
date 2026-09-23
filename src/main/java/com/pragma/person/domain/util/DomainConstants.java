package com.pragma.person.domain.util;

import java.time.ZoneId;

public final class DomainConstants {

    public static final int MAX_ACTIVE_BOOTCAMPS = 5;
    public static final ZoneId ZONE_ID = ZoneId.of("America/Bogota");

    private DomainConstants() {
    }
}
