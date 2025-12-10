package com.er.zoo.mapper;

import com.er.zoo.dto.*;
import com.er.zoo.model.Animal;
import com.er.zoo.model.Room;

/**
 * Mapper interface for converting between {@link Animal} entities and
 * Data Transfer Objects used in the Zoo API.
 */


public class Mapper {

    public static Animal toEntity(AnimalCreateRequest request) {
        return new Animal(
                request.title(),
                request.located()
        );
    }
    public static Animal toEntity(AnimalUpdateRequest request) {
        return new Animal(
                request.title(),
                request.located()
        );
    }

    public static AnimalResponse toResponse(Animal entity) {
        return new AnimalResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getLocated(),
                entity.getCreated(),
                entity.getUpdated(),
                entity.getRoomId(),
                entity.getFavoriteRoomIds(),
                Long.toString(entity.getVersion())
        );
    }

    public static Room toEntity(RoomCreateRequest request) {
        return new Room(
                request.title()
        );
    }

    public static RoomResponse toResponse(Room entity) {
        return new RoomResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getCreated(),
                entity.getUpdated(),
                Long.toString(entity.getVersion())
        );
    }
}
