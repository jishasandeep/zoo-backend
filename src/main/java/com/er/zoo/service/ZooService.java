package com.er.zoo.service;

import com.er.zoo.exception.DuplicateRequestException;
import com.er.zoo.logging.LoggerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public abstract class ZooService {
    protected final IdempotencyService idempotencyService;
    protected final LoggerService logger;
    protected final String entityName;


    protected ZooService(IdempotencyService idempotencyService, LoggerService logger) {
        this.idempotencyService = idempotencyService;
        this.logger = logger;
        this.entityName = getClass().getSimpleName();
    }

    protected void registerKey(String idempotencyKey){
        boolean registered = idempotencyService.registerKey(idempotencyKey);
        if (!registered) {
            logger.warn(entityName,"CREATE", "Duplicate idempotency key: " + idempotencyKey);
            throw new DuplicateRequestException("Duplicate request: Idempotency key already used.");
        }
    }

    /**
     * Check version match via ETag / If-Match
     * @param currentVersion current entity version
     * @param ifMatch client version
     */
    public void validateIfMatch(Long currentVersion,String ifMatch){
        if (ifMatch != null) {
            long clientVersion;
            try {
                clientVersion = Long.parseLong(ifMatch.replace("\"", ""));

            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ETag format");
            }

            if (!clientVersionEquals(currentVersion, clientVersion)) {
                logger.warn(entityName, "CONCURRENCY",
                        String.format("ETag mismatch: client=%s, current=%s", clientVersion, currentVersion));
                throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
                        "Entity has been modified by another user");
            }
            logger.info(entityName, "VALIDATION", "ETag validation passed (If-Match=" + clientVersion + ")");
        }
    }

    private boolean clientVersionEquals(Long server, long client) {
        return server != null && server == client;
    }
}
