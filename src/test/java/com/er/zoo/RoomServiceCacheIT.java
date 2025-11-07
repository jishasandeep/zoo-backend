package com.er.zoo;

import com.er.zoo.dto.RoomResponse;
import com.er.zoo.dto.RoomUpdateRequest;
import com.er.zoo.logging.LoggerService;
import com.er.zoo.mapper.RoomMapper;
import com.er.zoo.model.Room;
import com.er.zoo.repository.RoomRepository;
import com.er.zoo.service.IdempotencyService;
import com.er.zoo.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;
import java.util.Optional;

import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@EnableCaching
@Import(RoomService.class)
class RoomServiceCacheIT {


    @Autowired
    private RoomService roomService;


    @MockBean
    private RoomRepository roomRepo;

    @MockBean
    private RoomMapper mapper;

    @MockBean
    private IdempotencyService idempotencyService;

    @MockBean
    private LoggerService loggerService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        cacheManager.getCacheNames().forEach(n -> Objects.requireNonNull(cacheManager.getCache(n)).clear());
        reset(roomRepo, mapper);
    }

    @Test
    @DisplayName("Verify @Cacheable on get")
    void shouldCacheRoomAfterFirstFetch() {
        var room = new Room();
        room.setId("R123");
        room.setTitle("Green");

        RoomResponse response = new RoomResponse("R123", "Green", null, null, "0");
        when(roomRepo.findById("R123")).thenReturn(Optional.of(room));
        when(mapper.toResponse(room)).thenReturn(response);

        // First call -> DB hit
        roomService.getRoom("R123");
        // Second call -> cached
        roomService.getRoom("R123");

        // Verify repository called only once
        verify(roomRepo, times(1)).findById("R123");
    }

    @Test
    @DisplayName("Verify @CacheEvict on delete()")
    void shouldEvictCacheAfterDelete() {
        var room = new Room();
        room.setId("R123");
        room.setTitle("Green");
        room.setVersion(1L);

        when(roomRepo.findById("R123")).thenReturn(Optional.of(room));

        // First call -> DB hit
        roomService.getRoom("R123");
        verify(roomRepo, times(1)).findById("R123");

        // Delete should evict cache
        roomService.delete("R123", String.valueOf(1L));

        // Fetch again should re-hit DB
        roomService.getRoom("R123");
        verify(roomRepo, times(3)).findById("R123");
    }

    @Test
    @DisplayName("Verify @CachePut on update()")
    void shouldUpdateCacheOnRoomUpdate() {
        var room = new Room();
        room.setId("R123");
        room.setTitle("Old Room");
        room.setVersion(1L);

        var updatedRoom = new Room();
        updatedRoom.setId("R123");
        updatedRoom.setTitle("New Room");
        updatedRoom.setVersion(2L);

        RoomResponse updatedResponse = new RoomResponse("R123","New Room",null,null,"2");
        when(roomRepo.findById("R123")).thenReturn(Optional.of(room));
        when(roomRepo.save(any(Room.class))).thenReturn(updatedRoom);
        when(mapper.toResponse(updatedRoom)).thenReturn(updatedResponse);

        // Preload cache
        roomService.get("R123");

        // Update room
        roomService.update("R123", new RoomUpdateRequest("New Room"), "1");

        // Fetch again - should return new title from updated object (not old cache)
        RoomResponse response = roomService.getRoom("R123");
        verify(roomRepo, atLeastOnce()).save(any());
        assert response.title().equals("New Room");
    }


}
