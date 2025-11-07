package com.er.zoo;

import com.er.zoo.model.Animal;
import com.er.zoo.model.Room;
import com.er.zoo.repository.AnimalRepository;
import com.er.zoo.repository.RoomRepository;
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
class RoomRepositoryIT {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0.2");

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void shouldSaveAndRetrieveRoom() {
        Room room = new Room("Room1");
        Room saved = roomRepository.save(room);
        assertNotNull(saved.getId());
        assertEquals("Room1", saved.getTitle());
        assertNotNull(saved.getCreated());
        assertNotNull(saved.getUpdated());
        assertTrue(roomRepository.findById(room.getId()).isPresent());
    }
}

