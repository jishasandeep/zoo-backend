package com.er.zoo.controller;


import com.er.zoo.dto.*;
import com.er.zoo.logging.LoggerService;
import com.er.zoo.service.AnimalService;
import com.er.zoo.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @MockBean
    private AnimalService animalService;

    @MockBean
    private LoggerService loggerService;

    @Autowired
    private ObjectMapper objectMapper;

    private RoomResponse roomResponse;
    private AnimalResponse animalResponse;

    @BeforeEach
    void setup() {
        roomResponse = new RoomResponse("r1", "Green", null,null,"1");
        animalResponse = new AnimalResponse("a1", "Lion",  LocalDate.now(),
                null, null,"R1",null,"1");
    }

    @Test
    void createRoom_ShouldReturn201() throws Exception {
        RoomCreateRequest request = new RoomCreateRequest("Green");
        when(roomService.create(any(), eq("key123"))).thenReturn(roomResponse);

        mockMvc.perform(post("/api/v1/rooms")
                        .header("Idempotency-Key", "key123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", URI.create("/rooms/" + roomResponse.id()).toString()))
                .andExpect(jsonPath("$.title").value("Green"));

        verify(roomService).create(any(RoomCreateRequest.class), eq("key123"));
    }

    @Test
    void getRoom_ShouldReturnRoom() throws Exception {
        when(roomService.getRoom("r1")).thenReturn(roomResponse);

        mockMvc.perform(get("/api/v1/rooms/r1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Green"));
    }

    @Test
    void updateRoom_ShouldReturnUpdatedRoom() throws Exception {
        RoomUpdateRequest request = new RoomUpdateRequest("Big Room");
        when(roomService.update(eq("r1"), any(RoomUpdateRequest.class), eq("\"1\"")))
                .thenReturn(roomResponse);

        mockMvc.perform(put("/api/v1/rooms/r1")
                        .header("If-Match", "\"1\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Green"));

        verify(roomService).update(eq("r1"), any(RoomUpdateRequest.class), eq("\"1\""));
    }

    @Test
    void deleteRoom_ShouldReturnNoContent() throws Exception {
        doNothing().when(roomService).delete("r1", "\"1\"");

        mockMvc.perform(delete("/api/v1/rooms/r1")
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNoContent());

        verify(roomService).delete("r1", "\"1\"");
    }

    @Test
    void placeAnimal_ShouldReturnOk() throws Exception {
        when(animalService.assignToRoom("id1", "room1", "\"1\"")).thenReturn(animalResponse);

        mockMvc.perform(post("/api/v1/rooms/r1/animals/a1")
                        .header("If-Match", "\"1\""))
                .andExpect(status().isOk());

        verify(animalService).assignToRoom("a1", "r1", "\"1\"");
    }

    @Test
    void removeAnimal_ShouldReturnNoContent() throws Exception {
        doNothing().when(animalService).removeFromRoom("a1", "r1", "\"1\"");

        mockMvc.perform(delete("/api/v1/rooms/r1/animals/a1")
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNoContent());

        verify(animalService).removeFromRoom("a1", "r1", "\"1\"");
    }

    @Test
    void getAnimalsInRoom_ShouldReturnPagedAnimals() throws Exception {
        var page = new PageImpl<>(List.of(animalResponse), PageRequest.of(0, 10), 1);
        when(animalService.getAnimalsInRoom("r1", "title", "asc", 0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/v1/rooms/r1/animals")
                        .param("sort", "title")
                        .param("order", "asc")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Lion"));

        verify(animalService).getAnimalsInRoom("r1", "title", "asc", 0, 20);
    }


    @Test
    void favoriteRooms_ShouldReturnList() throws Exception {
        List<FavoriteRoomCount> favorites = List.of(
                new FavoriteRoomCount("Green", 4),
                new FavoriteRoomCount("Big", 1)
        );
        when(roomService.favoriteRoomCounts()).thenReturn(favorites);

        mockMvc.perform(get("/api/v1/rooms/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Green"))
                .andExpect(jsonPath("$[0].count").value(4));

        verify(roomService).favoriteRoomCounts();
    }
}
