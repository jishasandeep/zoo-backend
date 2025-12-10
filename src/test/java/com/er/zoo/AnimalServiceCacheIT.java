package com.er.zoo;

import com.er.zoo.dto.AnimalResponse;
import com.er.zoo.dto.AnimalUpdateRequest;
import com.er.zoo.dto.RoomRequest;
import com.er.zoo.enums.SortField;
import com.er.zoo.model.Animal;

import com.er.zoo.repository.AnimalRepository;
import com.er.zoo.repository.RoomRepository;
import com.er.zoo.logging.LoggerService;
import com.er.zoo.service.AnimalService;
import com.er.zoo.service.IdempotencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@EnableCaching
@Import(AnimalService.class)
class AnimalServiceCacheIT {

    @Autowired
    private AnimalService animalService;

    @MockBean
    private AnimalRepository animalRepo;

    @MockBean
    private RoomRepository roomRepo;


    @MockBean
    private IdempotencyService idempotencyService;

    @MockBean
    private LoggerService loggerService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        cacheManager.getCacheNames().forEach(n -> Objects.requireNonNull(cacheManager.getCache(n)).clear());
        reset(animalRepo, roomRepo);
    }

    @Test
    @DisplayName("Verify @Cacheable on getAnimal()")
    void shouldCacheAnimalAfterFirstFetch() {
        Animal animal = new Animal();
        animal.setId("a1");
        animal.setTitle("Lion");
        animal.setVersion(1L);

        when(animalRepo.findById("a1")).thenReturn(Optional.of(animal));

        // First call -> DB hit
        animalService.getAnimal("a1");
        // Second call -> cached
        animalService.getAnimal("a1");

        // Verify repository called only once
        verify(animalRepo, times(1)).findById("a1");
    }

    @Test
    @DisplayName("Verify @CacheEvict on delete()")
    void shouldEvictCacheAfterDelete() {
        Animal animal = new Animal();
        animal.setId("a2");
        animal.setVersion(1L);

        when(animalRepo.findById("a2")).thenReturn(Optional.of(animal));

        // Load into cache
        animalService.getAnimal("a2");
        verify(animalRepo, times(1)).findById("a2");

        // Delete should evict cache
        animalService.delete("a2", String.valueOf(1L));

        // Fetch again should re-hit DB
        animalService.getAnimal("a2");
        verify(animalRepo, times(3)).findById("a2");
    }

    @Test
    @DisplayName("Verify @CachePut on update()")
    void shouldUpdateCacheOnAnimalUpdate() {
        Animal existing = new Animal();
        existing.setId("a3");
        existing.setVersion(1L);
        existing.setTitle("Tiger");

        Animal updated = new Animal();
        updated.setId("a3");
        updated.setVersion(2L);
        updated.setTitle("Updated Tiger");


        when(animalRepo.findById("a3")).thenReturn(Optional.of(existing));
        when(animalRepo.save(ArgumentMatchers.any(Animal.class))).thenReturn(updated);

        // Cache initial
        animalService.getAnimal("a3");

        // Update - should refresh cache
        animalService.update("a3", new AnimalUpdateRequest("Updated Tiger", null), "1");

        // Fetch again - should return new title from updated object (not old cache)
        AnimalResponse response = animalService.getAnimal("a3");
        verify(animalRepo, atLeastOnce()).save(any());
        assert response.title().equals("Updated Tiger");
    }

    @Test
    @DisplayName("Verify @Cacheable on getAnimalsInRoom()")
    void shouldCacheAnimalsInRoomResult() {
        Animal a1 = new Animal();
        a1.setId("a1");
        a1.setTitle("Zebra");
        a1.setVersion(1L);
        Page<Animal> page = new PageImpl<>(List.of(a1));
        RoomRequest roomRequest = new RoomRequest("r1", SortField.TITLE, Sort.Direction.ASC, 0, 10);

        when(animalRepo.findByRoomId(eq("r1"), any(Pageable.class))).thenReturn(page);

        // First call hits DB
        animalService.getAnimalsInRoom(roomRequest);
        // Second call cached
        animalService.getAnimalsInRoom(roomRequest);

        // Verify DB hit only once
        verify(animalRepo, times(1)).findByRoomId(eq("r1"), any(Pageable.class));
    }
}
