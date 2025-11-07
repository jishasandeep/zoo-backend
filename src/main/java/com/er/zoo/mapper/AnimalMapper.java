package com.er.zoo.mapper;

import com.er.zoo.dto.AnimalCreateRequest;
import com.er.zoo.dto.AnimalUpdateRequest;
import com.er.zoo.dto.AnimalResponse;
import com.er.zoo.model.Animal;
import org.springframework.stereotype.Component;
/**
 * Mapper interface for converting between {@link Animal} entities and
 * Data Transfer Objects used in the Zoo API.
 */

@Component
public class AnimalMapper{

    public Animal toEntity(AnimalCreateRequest request) {
        return new Animal(
                request.title(),
                request.located()
        );
    }
    public Animal toEntity(AnimalUpdateRequest request) {
        return new Animal(
                request.title(),
                request.located()
        );
    }

    public AnimalResponse toResponse(Animal entity) {
        return new AnimalResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getLocated(),
                entity.getCreated(),
                entity.getUpdated(),
                entity.getRoomId(),
                entity.getFavoriteRoomIds(),
                entity.getVersion().toString()
        );
    }
}
