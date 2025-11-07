package com.er.zoo.repository;

import com.er.zoo.model.IdempotencyRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface IdempotencyRepository extends MongoRepository<IdempotencyRecord, String> {
    Optional<IdempotencyRecord> findByKey(String key);
}