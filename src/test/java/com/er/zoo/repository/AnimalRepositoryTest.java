package com.er.zoo.repository;

import com.er.zoo.model.Animal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link AnimalRepository}.
 *
 * <p>Uses an embedded MongoDB instance to verify CRUD and query operations.</p>
 */
@DataMongoTest
@ActiveProfiles("test")
class AnimalRepositoryTest {

    @Autowired
    private AnimalRepository animalRepository;

    @Test
    @DisplayName("Should save and find Animal by ID")
    void shouldSaveAndFindAnimalById() {
        // Arrange
        Animal animal = new Animal();
        animal.setTitle("Lion");
        animal.setLocated(LocalDate.now());

        // Act
        Animal saved = animalRepository.save(animal);
        assertNotNull(saved);
        assertNotNull(saved.getId());
        Animal found = animalRepository.findById(saved.getId()).orElse(null);

        // Assert
        assertNotNull(found,"Animal should exist in the database");
        assertEquals("Lion", found.getTitle());
        assertNotNull(found.getLocated());

    }

    @Test
    @DisplayName("Should find animals by roomId")
    void shouldFindAnimalsByRoomId() {
        // Arrange
        Animal tiger = new Animal();
        tiger.setTitle("Tiger");
        tiger.setRoomId("room-101");
        tiger.setCreated(Instant.now());
        tiger.setUpdated(Instant.now());
        tiger.setLocated(LocalDate.now());

        Animal cat = new Animal();
        cat.setTitle("Cat");
        cat.setRoomId("room-102");
        cat.setCreated(Instant.now());
        cat.setUpdated(Instant.now());
        cat.setLocated(LocalDate.now());

        animalRepository.saveAll(List.of(tiger, cat));

        // Act
        List<Animal> found = animalRepository.findByRoomId("room-101", null).getContent();

        // Assert
        assertEquals(1, found.size());
        assertEquals("Tiger", found.get(0).getTitle());
    }

    @Test
    @DisplayName("Should delete an Animal by ID")
    void shouldDeleteAnimalById() {
        // Arrange
        Animal dog = new Animal();
        dog.setTitle("Dog");
        dog.setCreated(Instant.now());
        dog.setUpdated(Instant.now());
        dog.setLocated(LocalDate.now());

        Animal saved = animalRepository.save(dog);
        assertTrue(animalRepository.findById(saved.getId()).isPresent());

        // Act
        animalRepository.deleteById(saved.getId());

        // Assert
        assertFalse(animalRepository.findById(saved.getId()).isPresent());
    }
}
