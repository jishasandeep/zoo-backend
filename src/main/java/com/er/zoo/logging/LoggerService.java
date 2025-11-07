package com.er.zoo.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
/**
 * Service interface for logging messages in the Zoo API.
 * <p>
 * Provides methods to log messages at different levels (info, warn, error)
 * with optional exception details. Implementations can log to console, file, or
 * external logging systems.
 * </p>
 * <p>
 * This service is intended to be used across services, controllers, and exception handlers
 * to maintain consistent logging and tracing of operations.
 * </p>
 */

@Service
public class LoggerService {

    private static final Logger log = LoggerFactory.getLogger(LoggerService.class);

    public void info(String entity, String action, String message) {
        log.info("[{}][{}] {}", entity.toUpperCase(), action.toUpperCase(), message);
    }

    public void warn(String entity, String action, String message) {
        log.warn("[{}][{}] {}", entity.toUpperCase(), action.toUpperCase(), message);
    }

    public void error(String entity, String action, String message, Throwable t) {
        log.error("[{}][{}] {} - {}", entity.toUpperCase(), action.toUpperCase(), message, t.getMessage(), t);
    }

    public void audit(String entity, String action, String user, String idempotencyKey) {
        log.info("[AUDIT][{}][{}] user={} idempotencyKey={} timestamp={}",
                entity.toUpperCase(), action.toUpperCase(), user, idempotencyKey, Instant.now());
    }
}

