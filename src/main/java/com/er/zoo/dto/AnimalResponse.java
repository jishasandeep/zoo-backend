package com.er.zoo.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

public record AnimalResponse(String id,
                             String title,
                             LocalDate located,
                             Instant created,
                             Instant updated,
                             String roomId,
                             Set<String> favoriteRoomIds,
                             String version
                             ) {}
