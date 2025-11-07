package com.er.zoo.dto;

import java.time.Instant;
import java.time.LocalDate;

public record RoomResponse(String id,
                           String title,
                           Instant created,
                           Instant updated,
                           String version
                             ) {}
