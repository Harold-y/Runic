package org.hye.util;

import java.util.UUID;

public class UUIDUtil {

    public static String generateUUID()
    {
        return String.valueOf(UUID.randomUUID());
    }
}
