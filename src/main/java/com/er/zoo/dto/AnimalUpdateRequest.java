package com.er.zoo.dto;

import java.time.LocalDate;

public record AnimalUpdateRequest(String title, LocalDate located) {}
