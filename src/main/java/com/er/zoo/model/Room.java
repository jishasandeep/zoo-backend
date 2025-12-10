package com.er.zoo.model;

import com.er.zoo.model.common.BaseDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Room document representing a cage/area in a zoo.
 */
@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "rooms")
public class Room extends BaseDocument {

    private String title;
    private Set<String> favoritedByAnimalIds = new HashSet<>();
    public Room(String title) { this.title = title; }


    public Room(String id, String title, Instant created, Instant updated, Set<String> favoritedByAnimalIds, long version) {
        super(id,created,updated,version);
        this.title = title;
        this.favoritedByAnimalIds = favoritedByAnimalIds;
    }
}
