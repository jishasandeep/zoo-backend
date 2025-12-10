package com.er.zoo.service;

import com.er.zoo.dto.FavoriteRoomCount;
import com.er.zoo.dto.RoomCreateRequest;
import com.er.zoo.dto.RoomResponse;
import com.er.zoo.dto.RoomUpdateRequest;
import com.er.zoo.logging.LoggerService;
import com.er.zoo.mapper.Mapper;
import com.er.zoo.model.Room;
import com.er.zoo.repository.RoomRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing {@link Room} entities in the Zoo API.
 * <p>
 * Provides CRUD operations, favorite room aggregation, and idempotency handling
 * for write operations.
 * </p>
 * <p>
 * This service extends {@link ZooService} to leverage shared functionality such as
 * idempotency registration and logging.
 * </p>
 * <p>
 */

@Service
public class RoomService extends ZooService{
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository,
                       IdempotencyService idempotencyService,
                       LoggerService loggerService) {
        super(idempotencyService, loggerService);
        this.roomRepository = roomRepository;
    }

    public Room create(RoomCreateRequest request, String idempotencyKey) {
        registerKey(idempotencyKey);
        return roomRepository.save(Mapper.toEntity(request));
    }
    @Cacheable(value = "rooms", key = "#id")
    public RoomResponse getRoom(String id) { return Mapper.toResponse(get(id)); }

    @CircuitBreaker(name = "roomService", fallbackMethod = "fallbackGetRoom")
    public Room get(String id) { return roomRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Room not found")); }

    // --- Fallback method ---
    public Room fallbackGetRoom(String id, Throwable ex) {
        logger.info("RoomService","fallbackGetRoom", "Fallback triggered for get(" + id + "): " + ex.getMessage());
        Room placeholder = new Room();
        placeholder.setId(id);
        placeholder.setTitle("Unavailable Room");
        return placeholder;
    }

    @CachePut(value = "rooms", key = "#id")
    public RoomResponse update(String id, RoomUpdateRequest updateRequest, String ifMatch) {
        var room = get(id);
        validateIfMatch(room.getVersion(),ifMatch);
        if(updateRequest.title()!= null)
            room.setTitle(updateRequest.title());
        return Mapper.toResponse(roomRepository.save(room));
    }

    @CacheEvict(value = "rooms", key = "#id")
    public void delete(String id, String ifMatch) {
        var room = get(id);
        validateIfMatch(room.getVersion(),ifMatch);
        roomRepository.deleteById(id);
    }


    @Transactional(readOnly = true)
    public List<FavoriteRoomCount> favoriteRoomCounts() {
        return roomRepository.findFavoriteRoomsWithCounts().stream()
                .map(p -> new FavoriteRoomCount(p.title(), p.favCount()))
                .collect(Collectors.toList());
    }
}
