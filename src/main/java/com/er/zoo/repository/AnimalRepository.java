package com.er.zoo.repository;

import com.er.zoo.model.Animal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AnimalRepository extends MongoRepository<Animal, String> {
    Page<Animal> findByRoomId(String roomId, Pageable pageable);
}
