package com.er.zoo.repository;

import com.er.zoo.dto.RoomCountProjection;
import com.er.zoo.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Aggregation;

import java.util.List;
/**
 * Repository interface for {@link Room} entities.
 * <p>
 * Provides basic CRUD operations through {@link MongoRepository} as well as
 * custom queries for retrieving aggregated information about favorite rooms.
 * </p>
 */
public interface RoomRepository extends MongoRepository<Room, String> {
    @Aggregation(pipeline = {
            "{ '$project': { title: 1, favCount: { $size: { $ifNull: [ '$favoritedByAnimalIds', [] ] } } } }",
            "{ '$match': { favCount: { $gt: 0 } } }",
            "{ '$project': { title: 1, favCount: 1, _id: 0 } }"
    })
    List<RoomCountProjection> findFavoriteRoomsWithCounts();


}
