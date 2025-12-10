package com.er.zoo.controller;

import com.er.zoo.dto.*;
import com.er.zoo.logging.LoggerService;
import com.er.zoo.service.AnimalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnimalController.class)
class AnimalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnimalService animalService;

    @MockBean
    private LoggerService loggerService;

    @Autowired
    private ObjectMapper objectMapper;

    private AnimalResponse response;

    @BeforeEach
    void setup() {
        response = new AnimalResponse("id1", "Lion", LocalDate.now(),
                null, null,"R1",null,"1");
    }

    @Test
    void createAnimal_ShouldReturn201() throws Exception {
        var request = new AnimalCreateRequest("Lion", LocalDate.now());
        when(animalService.create(any(), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "key123")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Lion"));

        verify(animalService).create(any(), eq("key123"));
    }

    // ---------- GET ----------
    @Test
    void getAnimal_ShouldReturnAnimal() throws Exception {
        when(animalService.getAnimal("id1")).thenReturn(response);

        mockMvc.perform(get("/api/v1/animals/id1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Lion"));
    }

    @Test
    void updateAnimal_ShouldReturnUpdatedResponse() throws Exception {
        var updateReq = new AnimalUpdateRequest("Tiger", LocalDate.now());
        when(animalService.update(eq("id1"), any(), anyString())).thenReturn(response);

        mockMvc.perform(put("/api/v1/animals/id1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "\"1\"")
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Lion"));

        verify(animalService).update(eq("id1"), any(), eq("\"1\""));
    }

    @Test
    void deleteAnimal_ShouldReturnNoContent() throws Exception {
        doNothing().when(animalService).delete("id1", "\"1\"");

        mockMvc.perform(delete("/api/v1/animals/id1")
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNoContent());

        verify(animalService).delete("id1", "\"1\"");
    }

    @Test
    void assignToRoom_ShouldReturnUpdatedAnimal() throws Exception {
        when(animalService.assignToRoom("id1", "room1", "\"1\"")).thenReturn(response);

        mockMvc.perform(post("/api/v1/animals/id1/move/room1")
                        .header("If-Match", "\"1\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Lion"));

        verify(animalService).assignToRoom("id1", "room1", "\"1\"");
    }

/*    @Test
    void removeFromRoom_ShouldReturnNoContent() throws Exception {
        doNothing().when(animalService).removeFromRoom("id1", "room1", "\"1\"");

        mockMvc.perform(delete("/api/v1/animals/id1/room/room1")
                        .header("If-Match", "\"1\""))
                .andExpect(status().isNoContent());

        verify(animalService).removeFromRoom("id1", "room1", "\"1\"");
    }*/

    @Test
    void assignFavorites_ShouldReturnUpdatedAnimal() throws Exception {
        var favRoomsRequest = new FavouriteRoomsRequest(List.of("r1", "r2"));
        when(animalService.assignFavorite(eq("id1"), anyList(), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/animals/id1/favorites")
                        .header("If-Match", "\"1\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(favRoomsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Lion"));

        verify(animalService).assignFavorite(eq("id1"), anyList(), eq("\"1\""));
    }

    @Test
    void unassignFavorites_ShouldReturnUpdatedAnimal() throws Exception {
        var favRoomsRequest = new FavouriteRoomsRequest(List.of("r1"));
        when(animalService.unassignFavorite(eq("id1"), anyList(), anyString())).thenReturn(response);

        mockMvc.perform(delete("/api/v1/animals/id1/favorites")
                        .header("If-Match", "\"1\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(favRoomsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Lion"));

        verify(animalService).unassignFavorite(eq("id1"), anyList(), eq("\"1\""));
    }

   /* @Test
    void getAnimalsInRoom_ShouldReturnPagedResponse() throws Exception {
        var page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);
        when(animalService.getAnimalsInRoom("room1", null, "asc", 0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/v1/animals/room/room1")
                        .param("order", "asc")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Lion"));

        verify(animalService).getAnimalsInRoom("room1", null, "asc", 0, 10);
    }*/
}
