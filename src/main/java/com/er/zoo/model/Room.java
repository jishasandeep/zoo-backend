package com.er.zoo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Room document representing a cage/area in a zoo.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "rooms")
public class Room {
    @Id
    private String id;
    private String title;

    @CreatedDate
    private Instant created;
    @LastModifiedDate
    private Instant updated;

    private Set<String> favoritedByAnimalIds = new HashSet<>();

    @Version
    private Long version;

    public Room(String title) { this.title = title; }

}
