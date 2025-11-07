package com.er.zoo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
/**
 * Represents a record of an idempotency key for the Zoo API.
 * <p>
 * This entity is used to enforce idempotency for write operations such as
 * creating animals or rooms.
 * </p>

 * <p>
 * <b>Important:</b> When using the same idempotency key across multiple API endpoints (e.g., Animal create and Room create),
 * it is recommended that the client prefixes the key with an API-specific identifier.
 * This avoids conflicts where the same key might otherwise be interpreted as a duplicate request
 * across different APIs.
 * <br>
 * Example:
 * <pre>
 * Animal create request:   Idempotency-Key: ANIMAL-123456
 * Room create request:     Idempotency-Key: ROOM-123456
 * </pre>
 * </p>
 * <p>
 * Fields:
 * <ul>
 *     <li>{@link #key} — The client-provided idempotency key (should include API prefix if reused across APIs).</li>
 *     <li>{@link #createdAt} — Timestamp when the record was created.</li>
 * </ul>
 * </p>
 */
@Data
@Document(collection = "idempotency")
public class IdempotencyRecord {

    @Id
    private String key;

    @Indexed(expireAfterSeconds = 86400)
    private Instant createdAt = Instant.now();

    public IdempotencyRecord(String key) {
        this.key = key;
    }


}
