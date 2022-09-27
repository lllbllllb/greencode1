package com.lllbllllb.greencode;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Util {

    public static String getStringBySeed(int seed) {
        return Integer.toString(seed);
    }

    public static UUID getUuidBySeed(int seed) {
        return UUID.nameUUIDFromBytes(getStringBySeed(seed).getBytes());
    }

}
