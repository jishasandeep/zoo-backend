package com.er.zoo.service;

import com.er.zoo.dto.AnimalCreateRequest;
import com.er.zoo.dto.AnimalResponse;
import com.er.zoo.dto.AnimalUpdateRequest;
import com.er.zoo.dto.RoomRequest;
import com.er.zoo.enums.SortField;
import com.er.zoo.exception.AnimalNotFoundException;
import com.er.zoo.logging.LoggerService;
import com.er.zoo.model.Animal;
import com.er.zoo.model.Room;
import com.er.zoo.repository.AnimalRepository;
import com.er.zoo.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AnimalService}.
 * Tests  the service logic using Mockito
 */
class AnimalServiceTest {

    @Mock private AnimalRepository animalRepo;
    @Mock private RoomRepository roomRepo;
    @Mock private IdempotencyService idempotencyService;
    @Mock private LoggerService loggerService;

    @InjectMocks
    private AnimalService animalService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should create animal successfully and return response")
    void create_shouldCreateAnimalSuccessfully() {
        AnimalCreateRequest request = new AnimalCreateRequest("Tiger", LocalDate.parse("2025-11-05"));
        Animal entity = new Animal();
        entity.setId("A1");
        entity.setTitle("Tiger");
        entity.setVersion(1L);
        entity.setLocated(LocalDate.parse("2025-11-05"));

        when(idempotencyService.registerKey(any())).thenReturn(true);
        when(animalRepo.save(any())).thenReturn(entity);

        AnimalResponse result = animalService.create(request, "idemp-animal-1");

        assertNotNull(result);
        assertEquals("Tiger", result.title());
        verify(animalRepo, times(1)).save(entity);
    }

    @Test
    @DisplayName("Should get animal by ID and map to response")
    void get_shouldGetAnimalById() {
        Animal entity = new Animal();
        entity.setId("A1");
        entity.setTitle("Lion");
        entity.setVersion(1L);


        when(animalRepo.findById("A1")).thenReturn(Optional.of(entity));

        AnimalResponse result = animalService.getAnimal("A1");

        assertEquals("Lion", result.title());
        verify(animalRepo).findById("A1");
    }

    @Test
    @DisplayName("Should throw when animal not found")
    void get_shouldThrowWhenAnimalNotFound() {
        when(animalRepo.findById("999")).thenReturn(Optional.empty());
        assertThrows(AnimalNotFoundException.class, () -> animalService.get("999"));
    }

    @Test
    @DisplayName("Should update existing animal with new values")
    void update_shouldUpdateAnimal() {
        String id = "A2";
        String ifMatch = "1";
        Animal existing = new Animal();
        existing.setId(id);
        existing.setTitle("OldTitle");
        existing.setVersion(1L);
        AnimalUpdateRequest update = new AnimalUpdateRequest("NewTitle", LocalDate.parse("2025-11-05"));

        when(animalRepo.findById(id)).thenReturn(Optional.of(existing));
        when(animalRepo.save(existing)).thenReturn(existing);

        AnimalResponse result = animalService.update(id, update, ifMatch);

        assertEquals("NewTitle", result.title());
        verify(animalRepo).save(existing);
    }

    @Test
    @DisplayName("Should delete animal when If-Match matches")
    void delete_shouldDeleteAnimal() {
        Animal existing = new Animal();
        existing.setId("A3");
        existing.setVersion(2L);
        when(animalRepo.findById("A3")).thenReturn(Optional.of(existing));

        animalService.delete("A3", "2");

        verify(animalRepo, times(1)).deleteById("A3");
    }

    @Test
    @DisplayName("Should assign animal to room if both exist")
    void shouldAssignToRoom() {
        Animal animal = new Animal();
        animal.setId("A4");
        animal.setVersion(1L);
        Room room = new Room();
        room.setId("R1");
        room.setVersion(1L);


        when(animalRepo.findById("A4")).thenReturn(Optional.of(animal));
        when(roomRepo.findById("R1")).thenReturn(Optional.of(room));
        when(animalRepo.save(animal)).thenReturn(animal);

        AnimalResponse result = animalService.assignToRoom("A4", "R1", "1");

        assertEquals("R1", result.roomId());
        verify(animalRepo).save(animal);
    }

    @Test
    @DisplayName("Should throw if room not found when assigning")
    void shouldThrowIfRoomNotFound() {
        Animal animal = new Animal();
        animal.setId("A5");
        animal.setVersion(1L);
        when(animalRepo.findById("A5")).thenReturn(Optional.of(animal));
        when(roomRepo.findById("XYZ")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> animalService.assignToRoom("A5", "XYZ", "1"));
    }

    @Test
    @DisplayName("Should return animals in room paginated and sorted")
    void shouldReturnAnimalsInRoom() {
        Animal entity1 = new Animal();
        entity1.setId("A6");
        entity1.setTitle("Elephant");
        entity1.setVersion(1L);

        Animal entity2 = new Animal();
        entity2.setId("A7");
        entity2.setTitle("Tiger");
        entity2.setVersion(1L);
        RoomRequest roomRequest = new RoomRequest("R2", SortField.TITLE, Sort.Direction.ASC, 0, 5);

        Page<Animal> page = new PageImpl<>(List.of(entity1,entity2));
        when(animalRepo.findByRoomId(eq("R2"), any(Pageable.class))).thenReturn(page);
        Page<AnimalResponse> result = animalService.getAnimalsInRoom(roomRequest);

        assertEquals(2, result.getTotalElements());
        assertEquals("Elephant", result.getContent().get(0).title());
        assertEquals("Tiger", result.getContent().get(1).title());
    }

    @Test
    @DisplayName("Should throw exception when roomIds contain invalid IDs in favorites")
    void shouldThrowIfInvalidRoomIds() {
        Animal animal = new Animal();
        animal.setId("A7");
        animal.setVersion(1L);

        when(animalRepo.findById("A7")).thenReturn(Optional.of(animal));
        when(roomRepo.findAllById(anyList())).thenReturn(List.of(new Room("R1","Room1", Instant.now(), Instant.now(), new HashSet<>(),0L)));

        List<String> inputIds = List.of("R1", "R2"); // R2 missing

        assertThrows(IllegalArgumentException.class,
                () -> animalService.assignFavorite("A7", inputIds, "1"));
    }


}
