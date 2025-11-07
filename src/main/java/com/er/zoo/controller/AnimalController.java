package com.er.zoo.controller;

import com.er.zoo.dto.AnimalCreateRequest;
import com.er.zoo.dto.AnimalUpdateRequest;
import com.er.zoo.dto.AnimalResponse;
import com.er.zoo.dto.FavouriteRoomsRequest;
import com.er.zoo.service.AnimalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
/**
 * REST controller for managing Animals in the Zoo.
 * <p>
 * Provides endpoints to create, retrieve, update, and delete animals,
 * assign or remove animals to/from rooms, and manage favorite rooms.
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
@RequestMapping("/api/v1/animals")
@Tag(
        name = "Animals API",
        description = "Operations for managing animals in the zoo, including creation, updates, retrieval, and deletion."
)
public class AnimalController {
    private final AnimalService animalService;


    public AnimalController(AnimalService animalService) { this.animalService = animalService; }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<AnimalResponse> create(@Valid @RequestBody AnimalCreateRequest request,
                                                 @RequestHeader("Idempotency-Key") String idempotencyKey) {
        var saved = animalService.create(request,idempotencyKey);
        return ResponseEntity
                .created(URI.create("api/v1/animals/" + saved.id()))
                .eTag(saved.version())
                .body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponse> get(@PathVariable String id) {
        AnimalResponse animalResponse = animalService.getAnimal(id);
        return ResponseEntity.ok()
                .eTag(animalResponse.version())
                .body(animalResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimalResponse> update(@PathVariable String id,
                                                 @Valid @RequestBody AnimalUpdateRequest animalUpdateRequest,
                                                 @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        AnimalResponse animalResponse = animalService.update(id, animalUpdateRequest, ifMatch);
        return ResponseEntity.ok()
                .eTag(animalResponse.version())
                .body(animalResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id,
                                       @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        animalService.delete(id,ifMatch);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{animalId}/move/{roomId}")
    public ResponseEntity<AnimalResponse> move(@PathVariable String animalId,
                                               @PathVariable String roomId,
                                               @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        return ResponseEntity.ok(animalService.assignToRoom(animalId, roomId, ifMatch));
    }

    @PostMapping("/{animalId}/favorites")
    public ResponseEntity<AnimalResponse> assignFavorite(@PathVariable String animalId,
                                                         @RequestBody FavouriteRoomsRequest favouriteRoomsRequest,
                                                         @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        return ResponseEntity.ok(animalService.assignFavorite(animalId, favouriteRoomsRequest.roomIds(),ifMatch));
    }

    @DeleteMapping("/{animalId}/favorites")
    public ResponseEntity<AnimalResponse> unassignFavorite(@PathVariable String animalId,
                                                           @RequestBody FavouriteRoomsRequest favouriteRoomsRequest,
                                                           @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        return ResponseEntity.ok(animalService.unassignFavorite(animalId, favouriteRoomsRequest.roomIds(),ifMatch));
    }
}
