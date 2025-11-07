package com.er.zoo.service;

import com.er.zoo.model.IdempotencyRecord;
import com.er.zoo.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRepository repository;

    /**
     * Tries to register a new idempotency key.
     * @return true if successfully registered, false if key already exists.
     */
    public boolean registerKey(String key) {
        try {
            if(repository.existsById(key))
                return false;
            repository.save(new IdempotencyRecord(key));
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
