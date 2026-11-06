package com.example.jojoaddon.client;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Log de diagnostico, uma vez por chave (evita spam no latest.log). */
public final class AnimDebug {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> SEEN = ConcurrentHashMap.newKeySet();

    private AnimDebug() {}

    public static void once(String key, String message) {
        if (SEEN.add(key)) LOGGER.info(message);
    }
}
