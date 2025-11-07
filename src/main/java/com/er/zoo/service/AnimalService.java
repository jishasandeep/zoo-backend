package com.er.zoo.service;

import com.er.zoo.dto.AnimalCreateRequest;
import com.er.zoo.dto.AnimalUpdateRequest;
import com.er.zoo.dto.AnimalResponse;
import com.er.zoo.logging.LoggerService;
import com.er.zoo.mapper.AnimalMapper;
import com.er.zoo.model.Animal;
import com.er.zoo.model.Room;
import com.er.zoo.repository.AnimalRepository;
import com.er.zoo.repository.RoomRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
/**
 * Service class for managing {@link Animal} entities in the Zoo API.
 * <p>
 * Provides CRUD operations, room assignment, favorite room management,
 * and caching of frequently accessed data to improve performance.
 * </p>
 * <p>
 * This service extends {@link ZooService} to leverage shared functionality,
 * such as idempotency handling and logging.
 * </p>
 */

@Service
public class AnimalService extends ZooService{
    private final AnimalRepository animalRepo;
    private final RoomRepository roomRepo;
    private final AnimalMapper mapper;

    public AnimalService(AnimalRepository animalRepo, RoomRepository roomRepo,
                         AnimalMapper mapper, IdempotencyService idempotencyService,
                         LoggerService loggerService) {
        super(idempotencyService, loggerService);
        this.animalRepo = animalRepo;
        this.roomRepo = roomRepo;
        this.mapper = mapper;
    }

    public AnimalResponse create(AnimalCreateRequest request, String idempotencyKey) {
        registerKey(idempotencyKey);
        return mapper.toResponse(animalRepo.save(mapper.toEntity(request)));
    }

    @Cacheable(value = "animals", key = "#id")
    public AnimalResponse getAnimal(String id) { return mapper.toResponse(get(id)); }

    public Animal get(String id) { return animalRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Animal not found")); }

    @CachePut(value = "animals", key = "#id")
    public AnimalResponse update(String id, AnimalUpdateRequest request, String ifMatch) {
        var existing = get(id);
        validateIfMatch(existing.getVersion(),ifMatch);
        if(request.title()!= null)
            existing.setTitle(request.title());
        if(request.located()!= null)
            existing.setLocated(request.located());
        return mapper.toResponse(animalRepo.save(existing));
    }

    @Caching(evict = {
            @CacheEvict(value = "animals", key = "#id"),
            @CacheEvict(value = "animalsInRoom", allEntries = true) //evict room lists when animals change
    })
    public void delete(String id, String ifMatch) {
        Animal existing = get(id);
        validateIfMatch(existing.getVersion(),ifMatch);
        animalRepo.deleteById(id); }

    @CachePut(value = "animals", key = "#animalId")
    @CacheEvict(value = "animalsInRoom", allEntries = true)
    @Transactional
    public AnimalResponse assignToRoom(String animalId, String roomId, String ifMatch) {
        var animal = get(animalId);
        validateIfMatch(animal.getVersion(),ifMatch);
        var room = roomRepo.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Room not found"));
        animal.setRoomId(room.getId());
        return mapper.toResponse(animalRepo.save(animal));
    }

    @CachePut(value = "animals", key = "#animalId")
    @CacheEvict(value = "animalsInRoom", allEntries = true)
    @Transactional
    public void removeFromRoom(String animalId, String roomId, String ifMatch) {
        var animal = get(animalId);
        validateIfMatch(animal.getVersion(),ifMatch);
        if (animal.getRoomId() == null || !animal.getRoomId().equals(roomId)) {
            throw new IllegalArgumentException("Animal not in specified room");
        }
        animal.setRoomId(null);
        animalRepo.save(animal);
    }


    @CachePut(value = "animals", key = "#animalId")
    @Transactional
    public AnimalResponse assignFavorite(String animalId, List<String> roomIds, String ifMatch) {

        var animal = get(animalId);
        validateIfMatch(animal.getVersion(),ifMatch);
        List<Room> existingRooms = getRooms(roomIds);
        Set<String> favourites = new HashSet<>(Optional.ofNullable(animal.getFavoriteRoomIds()).orElse(Set.of()));
        favourites.addAll(roomIds);

        animal.setFavoriteRoomIds(favourites);

        var saved = animalRepo.save(animal);
        // maintain reverse reference for fast aggregation
        existingRooms.forEach(it -> {
            it.getFavoritedByAnimalIds().add(animal.getId());
            roomRepo.save(it);
        });
        return mapper.toResponse(saved);
    }


    @CachePut(value = "animals", key = "#animalId")
    @Transactional
    public AnimalResponse unassignFavorite(String animalId, List<String> roomIds, String ifMatch) {
        var animal = get(animalId);
        validateIfMatch(animal.getVersion(),ifMatch);
        List<Room> existingRooms = getRooms(roomIds);
        roomIds.forEach(animal.getFavoriteRoomIds()::remove);
        var saved = animalRepo.save(animal);

        existingRooms.forEach(it->{
            it.getFavoritedByAnimalIds().remove(animalId);
            roomRepo.save(it);
        });

        return mapper.toResponse(saved);
    }
    @Cacheable(value = "animalsInRoom", key = "#roomId + ':' + #sortField + ':' + #order + ':' + #page + ':' + #size")
    public Page<AnimalResponse> getAnimalsInRoom(String roomId, String sortField, String order, int page, int size) {
        var dir = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        var sort = Sort.by(dir, sortField == null ? "title" : sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Animal> animals = animalRepo.findByRoomId(roomId, pageable);
        return animals.map(mapper::toResponse);
    }

    private List<Room> getRooms(List<String> roomIds){
        List<Room> existingRooms = roomRepo.findAllById(roomIds);
        List<String> existingIds = existingRooms.stream()
                .map(Room::getId)
                .toList();

        if (existingIds.size() != roomIds.size()) {
            Set<String> missing = new HashSet<>(roomIds);
            existingIds.forEach(missing::remove);
            throw new IllegalArgumentException("Invalid room IDs: " + missing);
        }
        return existingRooms;
    }
}
