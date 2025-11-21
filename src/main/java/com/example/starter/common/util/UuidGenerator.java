package com.example.starter.common.util;

import java.util.UUID;

public final class UuidGenerator {

    private UuidGenerator() {
    }

    public static UUID randomUuid() {
        return UUID.randomUUID();
    }
}
