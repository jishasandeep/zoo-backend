package com.er.zoo.repository;

import com.er.zoo.model.IdempotencyRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IdempotencyRepository extends MongoRepository<IdempotencyRecord, String> {
}