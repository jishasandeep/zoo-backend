package com.er.zoo.mapper;

import com.er.zoo.dto.*;
import com.er.zoo.model.Animal;
import com.er.zoo.model.Room;
import org.springframework.stereotype.Component;
/**
 * Mapper interface for converting between {@link Room} entities and
 * Data Transfer Objects used in the Zoo API.
 */
@Component
public class RoomMapper {

    public Room toEntity(RoomCreateRequest request) {
        return new Room(
                request.title()
        );
    }

    public RoomResponse toResponse(Room entity) {
        return new RoomResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getCreated(),
                entity.getUpdated(),
                entity.getVersion().toString()
        );
    }
}
