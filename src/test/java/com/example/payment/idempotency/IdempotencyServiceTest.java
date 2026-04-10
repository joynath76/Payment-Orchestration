package com.example.payment.idempotency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IdempotencyServiceTest {

    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        idempotencyService = new IdempotencyService();
        ReflectionTestUtils.setField(idempotencyService, "ttlMinutes", 60L);
    }

    @Test
    void shouldAcquireLockSuccessfully() {
        String key = "test-key";

        idempotencyService.validateAndLock(key);

        Object cache = ReflectionTestUtils.getField(idempotencyService, "cache");
        assertNotNull(cache);
        assertEquals(1, ((ConcurrentHashMap<?, ?>) cache).size());
    }

    @Test
    void shouldThrowExceptionWhenLockExists() {
        String key = "duplicate-key";

        idempotencyService.validateAndLock(key);

        assertThrows(IdempotencyException.class, () -> idempotencyService.validateAndLock(key));
    }

    @Test
    void shouldUpdateStatusSuccessfully() {
        String key = "test-key";
        String status = "SUCCESS";

        idempotencyService.updateStatus(key, status);

        Object cache = ReflectionTestUtils.getField(idempotencyService, "cache");
        assertNotNull(cache);
        assertEquals(1, ((ConcurrentHashMap<?, ?>) cache).size());
    }
}
