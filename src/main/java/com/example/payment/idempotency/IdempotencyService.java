package com.example.payment.idempotency;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class IdempotencyService {

    @Value("${app.idempotency.ttl-minutes:60}")
    private long ttlMinutes;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "idempotency-cache-cleaner");
        thread.setDaemon(true);
        return thread;
    });

    private static final String KEY_PREFIX = "payment_idempotency:";

    @PostConstruct
    void startCleanup() {
        cleanupExecutor.scheduleAtFixedRate(this::removeExpiredEntries, 1, Math.max(1, ttlMinutes), TimeUnit.MINUTES);
    }

    @PreDestroy
    void stopCleanup() {
        cleanupExecutor.shutdownNow();
    }

    public void validateAndLock(String key) {
        String cacheKey = KEY_PREFIX + key;
        long expiresAt = Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES).toEpochMilli();

        CacheEntry newEntry = new CacheEntry("IN_PROGRESS", expiresAt);

        CacheEntry existing = cache.compute(cacheKey, (k, old) -> {
            if (old == null || old.isExpired()) {
                return newEntry;
            }
            return old;
        });

        if (existing != newEntry) {
            log.warn("Idempotency violation detected for key: {}", key);
            throw new IdempotencyException("A request with this idempotency key is already being processed or has been completed.");
        }

        log.debug("Idempotency lock acquired for key: {}", key);
    }

    public void updateStatus(String key, String status) {
        String cacheKey = KEY_PREFIX + key;
        long expiresAt = Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES).toEpochMilli();
        cache.put(cacheKey, new CacheEntry(status, expiresAt));
        log.debug("Idempotency status updated for key: {} to {}", key, status);
    }

    private void removeExpiredEntries() {
        long now = Instant.now().toEpochMilli();
        cache.entrySet().removeIf(entry -> entry.getValue().expiresAt <= now);
    }

    private static final class CacheEntry {
        private final String status;
        private final long expiresAt;

        private CacheEntry(String status, long expiresAt) {
            this.status = status;
            this.expiresAt = expiresAt;
        }

        private boolean isExpired() {
            return Instant.now().toEpochMilli() >= expiresAt;
        }
    }
}
