package com.er.zoo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AnimalCreateRequest(@NotBlank String title, @NotNull LocalDate located) {}
