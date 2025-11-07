package com.er.zoo.dto;

import jakarta.validation.constraints.NotBlank;

public record RoomCreateRequest(@NotBlank String title) {}
