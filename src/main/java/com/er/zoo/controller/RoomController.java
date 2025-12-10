package com.er.zoo.controller;

import com.er.zoo.dto.*;
import com.er.zoo.model.Room;
import com.er.zoo.service.RoomService;
import com.er.zoo.service.AnimalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
/**
 * REST controller for managing Rooms in the Zoo.
 * <p>
 * Provides endpoints to create, retrieve, update, and delete rooms,
 * assign or remove animals from rooms, fetch animals in a specific room,
 * and list favorite rooms with counts.
 * </p>
 * <p>
 * All endpoints consume and produce JSON.
 * </p>
 *
 * @author Jisha Badi
 * @version 1.0
 * @since 2025-11-07
 */
@RestController
@RequestMapping("/api/v1/rooms")
@Tag(
        name = "Rooms API",
        description = "Operations for managing zoo rooms, including creation, updates, deletion, and animal assignments."
)
public class RoomController {
    private final RoomService roomService;
    private final AnimalService animalService;


    public RoomController(RoomService roomService, AnimalService animalService) {
        this.roomService = roomService; this.animalService = animalService;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Room> create(@Valid @RequestBody RoomCreateRequest request,
                                       @RequestHeader("Idempotency-Key") String idempotencyKey) {
        var saved = roomService.create(request,idempotencyKey);
        return ResponseEntity.created(URI.create("/rooms/" + saved.getId()))
                .eTag(saved.getVersion().toString())
                .body(saved);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public RoomResponse get(@PathVariable String id) { return roomService.getRoom(id); }

    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public RoomResponse update(@PathVariable String id,
                               @Valid @RequestBody RoomUpdateRequest updateRequest,
                               @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        return roomService.update(id, updateRequest, ifMatch);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id,
                                       @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        roomService.delete(id,ifMatch);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roomId}/animals/{animalId}")
    public ResponseEntity<Void> placeAnimal(@PathVariable String roomId,
                                            @PathVariable String animalId,
                                            @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        animalService.assignToRoom(animalId, roomId, ifMatch);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roomId}/animals/{animalId}")
    public ResponseEntity<Void> removeAnimal(@PathVariable String roomId, @PathVariable String animalId,
                                             @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        animalService.removeFromRoom(animalId, roomId, ifMatch);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roomId}/animals")
    public Page<AnimalResponse> getAnimalsInRoom(
            @Valid @ModelAttribute RoomRequest roomRequest
    ) {
        return animalService.getAnimalsInRoom(roomRequest);
    }

    @GetMapping("/favorites")
    public List<FavoriteRoomCount> favoriteRooms() {
        return roomService.favoriteRoomCounts();
    }
}
