package com.er.zoo.repository;

import static org.junit.jupiter.api.Assertions.*;
import com.er.zoo.model.Room;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;

/**
 * Unit tests for {@link RoomRepository}.
 *
 * <p>Uses an embedded MongoDB instance to verify
 * repository CRUD operations and queries.</p>
 */
@DataMongoTest
@ActiveProfiles("test")
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Test
    @DisplayName("Should save and find Room by ID")
    void shouldSaveAndFindRoomById() {
        // Arrange
        Room room = new Room();
        room.setTitle("Room1");
        room.setCreated(Instant.now());
        room.setUpdated(Instant.now());

        // Act
        Room saved = roomRepository.save(room);
        Optional<Room> found = roomRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent(), "Room should exist in the database");
        assertNotNull(found.get().getId());
        assertEquals("Room1", found.get().getTitle());
    }

    @Test
    @DisplayName("Should update an existing Room")
    void shouldUpdateExistingRoom() {
        // Arrange
        Room room = new Room();
        room.setTitle("Room2");
        room.setCreated(Instant.now());
        room.setUpdated(Instant.now());
        Room saved = roomRepository.save(room);

        // Act
        saved.setTitle("Green");
        Room updated = roomRepository.save(saved);

        // Assert
        assertEquals("Green", updated.getTitle());
        assertEquals(saved.getId(), updated.getId());
    }

    @Test
    @DisplayName("Should delete a Room by ID")
    void shouldDeleteRoomById() {
        // Arrange
        Room room = new Room();
        room.setTitle("Reptile House");
        room.setCreated(Instant.now());
        room.setUpdated(Instant.now());

        Room saved = roomRepository.save(room);
        assertTrue(roomRepository.findById(saved.getId()).isPresent());

        // Act
        roomRepository.deleteById(saved.getId());
        // Assert
        assertFalse(roomRepository.findById(saved.getId()).isPresent());
    }

    @Test
    @DisplayName("Should find all Rooms")
    void shouldFindAllRooms() {
        // Arrange
        roomRepository.deleteAll(); // clean slate
        Room r1 = new Room("Jungle Room");
        Room r2 = new Room("Ocean Zone");
        roomRepository.save(r1);
        roomRepository.save(r2);

        // Act
        var rooms = roomRepository.findAll();

        // Assert
        assertEquals(2, rooms.size());
    }
}
