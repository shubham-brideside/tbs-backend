package com.brideside.backend.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class IstTimestamps {

    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private IstTimestamps() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(IST);
    }
}
