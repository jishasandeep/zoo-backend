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
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Animal document stored in MongoDB.
 */
@Data
@Document(collection = "animals")
@AllArgsConstructor
@NoArgsConstructor
public class Animal {
    @Id
    private String id;
    private String title;

    @CreatedDate
    private Instant created;
    @LastModifiedDate
    private Instant updated;

    private LocalDate located;
    private String roomId;
    private Set<String> favoriteRoomIds = new HashSet<>();

    @Version
    private Long version;

    public Animal(String title, LocalDate located) {
        this.title = title;
        this.located = located;
    }


    }
