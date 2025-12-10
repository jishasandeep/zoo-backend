package com.er.zoo.model;

import com.er.zoo.model.common.BaseDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Animal document stored in MongoDB.
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Document(collection = "animals")
@AllArgsConstructor
@NoArgsConstructor
public class Animal extends BaseDocument {

    private String title;
    private LocalDate located;
    private String roomId;
    private Set<String> favoriteRoomIds = new HashSet<>();

    public Animal(String title, LocalDate located) {
        this.title = title;
        this.located = located;
    }


    }
