package com.er.zoo;

import com.er.zoo.model.Animal;
import com.er.zoo.repository.AnimalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class AnimalRepositoryIT {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0.2");

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private AnimalRepository animalRepository;

    @Test
    void shouldSaveAndRetrieveAnimal() {
        Animal a = new Animal();
        a.setTitle("Elephant");
        a.setLocated(LocalDate.now());
        Animal saved = animalRepository.save(a);
        assertNotNull(saved.getId());
        assertEquals("Elephant", saved.getTitle());
        assertNotNull(saved.getLocated());
        assertNotNull(saved.getCreated());
        assertNotNull(saved.getUpdated());
        assertTrue(animalRepository.findById(a.getId()).isPresent());
    }
}

