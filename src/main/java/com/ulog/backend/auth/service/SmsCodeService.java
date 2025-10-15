package com.ulog.backend.auth.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class SmsCodeService {

    private static final String DEFAULT_CODE = "123456";
    private static final long TTL_SECONDS = 300;

    private final Map<String, Long> codeStore = new ConcurrentHashMap<>();

    public void storeCode(String phone) {
        codeStore.put(phone, Instant.now().getEpochSecond());
    }

    public boolean verify(String phone, String code) {
        Long issuedAt = codeStore.computeIfAbsent(phone, key -> Instant.now().getEpochSecond());
        if (Instant.now().getEpochSecond() - issuedAt > TTL_SECONDS) {
            codeStore.put(phone, Instant.now().getEpochSecond());
            return false;
        }
        return DEFAULT_CODE.equals(code);
    }
}
