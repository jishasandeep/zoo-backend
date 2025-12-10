package com.er.zoo.dto;

import java.time.Instant;

public record RoomResponse(String id,
                           String title,
                           Instant created,
                           Instant updated,
                           String version
                             ) {}
