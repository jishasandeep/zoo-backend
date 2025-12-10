package com.er.zoo;

import com.er.zoo.model.Room;
import com.er.zoo.repository.AnimalRepository;
import com.er.zoo.repository.IdempotencyRepository;
import com.er.zoo.repository.RoomRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class ZooIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    AnimalRepository animalRepository;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    IdempotencyRepository idempotencyRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        idempotencyRepository.deleteAll();
        animalRepository.deleteAll();
        roomRepository.deleteAll();
    }

    @Test
    void createAndGetAnimal() throws Exception {
        var json = "{\"title\":\"Lion\",\"located\":\"2022-07-30\"}";
        MvcResult createResult = mvc.perform(post("/api/v1/animals")
                        .contentType(MediaType.APPLICATION_JSON).content(json)
                        .header("Idempotency-Key", "key1234"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Lion"))
                .andExpect(jsonPath("$.located").value("2022-07-30"))
                .andReturn();
        String animalId = extractId(createResult);

        mvc.perform(get("/api/v1/animals/"+animalId)).andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Lion"));
    }

    @Test
    void createAndGetRoom() throws Exception {
        var json = "{\"title\":\"Paradise\"}";
        mvc.perform(post("/api/v1/rooms").contentType(MediaType.APPLICATION_JSON).content(json)
                .header("Idempotency-Key", "key1234"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Paradise"));

        // create another and get one
        var r = new Room("Blue"); roomRepository.save(r);
        mvc.perform(get("/api/v1/rooms/" + r.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Blue"));
    }


    @Test
    void whenMissingIdempotencyKey_thenReturns400() throws Exception {
        var json = "{\"title\":\"Tiger\",\"located\":\"2022-07-30\"}";
        mvc.perform(post("/api/v1/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing required header"))
                .andExpect(jsonPath("$.message").value("Header 'Idempotency-Key' is required"));
    }

    @Test
    void whenTitleBlank_thenReturns400() throws Exception {
        var json = "{\"title\":\"\",\"located\":\"2022-07-30\"}";
        mvc.perform(post("/api/v1/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Idempotency-Key", "key-001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.title").value("must not be blank"));
    }

    @Test
    void whenTitleNull_thenReturns400() throws Exception {
        var json = "{\"located\":\"2022-07-30\"}";
        mvc.perform(post("/api/v1/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Idempotency-Key", "key-001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.title").value("must not be blank"));
    }

    @Test
    void createAnimalAndPlaceInRoom() throws Exception {
        var room = new Room("Green");
        roomRepository.save(room);
        var json = "{\"title\":\"Tiger\",\"located\":\"2022-07-30\"}";
        mvc.perform(post("/api/v1/animals")
                        .contentType(MediaType.APPLICATION_JSON).content(json)
                        .header("Idempotency-Key", "key1234"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Tiger"))
                .andExpect(jsonPath("$.located").value("2022-07-30"));

        var a = animalRepository.findAll().stream().findAny().orElseThrow();
        mvc.perform(post("/api/v1/animals/"+a.getId()+"/move/"+room.getId())).andExpect(status().isOk());
        mvc.perform(get("/api/v1/animals/"+a.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.roomId").value(room.getId()));
    }


    @Test
    void shouldReturnAnimalsAssignedToRoom_sort_title() throws Exception {
        // Step 1: Create a room
        var json = "{\"title\":\"Savannah\"}";
        MvcResult roomResult = mvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Idempotency-Key", "room-key-001"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        String roomId = extractId(roomResult);

        // Step 2: Create two animals
        var animal1 = "{\"title\":\"Lion\",\"located\":\"2022-07-30\"}";
        MvcResult lionResult = mvc.perform(post("/api/v1/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(animal1)
                        .header("Idempotency-Key", "animal-key-001"))
                .andExpect(status().isCreated())
                .andReturn();

        String lionId = extractId(lionResult);

        var animal2 = "{\"title\":\"Zebra\",\"located\":\"2022-07-30\"}";
        MvcResult zebraResult = mvc.perform(post("/api/v1/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(animal2)
                        .header("Idempotency-Key", "animal-key-002"))
                .andExpect(status().isCreated())
                .andReturn();

        String zebraId = extractId(zebraResult);

        // Step 3: Assign both animals to the room
        mvc.perform(post("/api/v1/rooms/{roomId}/animals/{animalId}", roomId, lionId)
                        .header("If-Match", "0"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/rooms/{roomId}/animals/{animalId}", roomId, zebraId)
                        .header("If-Match", "0"))
                .andExpect(status().isOk());

        // Step 4: Get all animals in the room
        MvcResult getAnimalsResult = mvc.perform(get("/api/v1/rooms/{roomId}/animals", roomId)
                        .param("sort", "TITLE")
                        .param("order", "ASC")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andReturn();

        // Step 5: Validate the returned animals
        String resultJson = getAnimalsResult.getResponse().getContentAsString();
        JsonNode page = objectMapper.readTree(resultJson);
        assertThat(page.get("content").get(0).get("title").asText()).isEqualTo("Lion");
        assertThat(page.get("content").get(1).get("title").asText()).isEqualTo("Zebra");
    }

    @Test
    void shouldReturnAnimalsAssignedToRoom_sort_located() throws Exception {
        // Step 1: Create a room
        var json = "{\"title\":\"Savannah\"}";
        MvcResult roomResult = mvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Idempotency-Key", "room-key-001"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        String roomId = extractId(roomResult);

        // Step 2: Create two animals
        var animal1 = "{\"title\":\"Lion\",\"located\":\"2022-07-30\"}";
        MvcResult lionResult = mvc.perform(post("/api/v1/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(animal1)
                        .header("Idempotency-Key", "animal-key-001"))
                .andExpect(status().isCreated())
                .andReturn();

        String lionId = extractId(lionResult);

        var animal2 = "{\"title\":\"Zebra\",\"located\":\"2025-10-25\"}";
        MvcResult zebraResult = mvc.perform(post("/api/v1/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(animal2)
                        .header("Idempotency-Key", "animal-key-002"))
                .andExpect(status().isCreated())
                .andReturn();

        String zebraId = extractId(zebraResult);

        // Step 3: Assign both animals to the room
        mvc.perform(post("/api/v1/rooms/{roomId}/animals/{animalId}", roomId, lionId)
                        .header("If-Match", "0"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/rooms/{roomId}/animals/{animalId}", roomId, zebraId)
                        .header("If-Match", "0"))
                .andExpect(status().isOk());

        // Step 4: Get all animals in the room
        MvcResult getAnimalsResult = mvc.perform(get("/api/v1/rooms/{roomId}/animals", roomId)
                        .param("sort", "LOCATED")
                        .param("order", "DESC")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andReturn();

        // Step 5: Validate the returned animals
        String resultJson = getAnimalsResult.getResponse().getContentAsString();
        JsonNode page = objectMapper.readTree(resultJson);
        assertThat(page.get("content").get(0).get("located").asText()).isEqualTo("2025-10-25");
        assertThat(page.get("content").get(1).get("located").asText()).isEqualTo("2022-07-30");
    }


    @Test
    void shouldListFavouriteRoomsWithCounts() throws Exception {
        // Step 1: Create three rooms
        var room1 = "{\"title\":\"Green\"}";
        var room2 = "{\"title\":\"Big\"}";
        var room3 = "{\"title\":\"Red\"}";

        MvcResult greenRoomResult = mvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(room1)
                        .header("Idempotency-Key", "room-green-key"))
                .andExpect(status().isCreated())
                .andReturn();

        String greenRoomId = extractId(greenRoomResult);

        MvcResult bigRoomResult = mvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(room2)
                        .header("Idempotency-Key", "room-big-key"))
                .andExpect(status().isCreated())
                .andReturn();

        String bigRoomId = extractId(bigRoomResult);

       mvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(room3)
                        .header("Idempotency-Key", "room-red-key"))
                .andExpect(status().isCreated())
                .andReturn();


        // Step 2: Create two animals
        var animal1 = "{\"title\":\"Tiger\",\"located\":\"2025-10-30\"}";
        var animal2 = "{\"title\":\"Lion\",\"located\":\"2025-10-30\"}";
        MvcResult tigerResult = mvc.perform(post("/api/v1/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(animal1)
                        .header("Idempotency-Key", "animal-tiger-key"))
                .andExpect(status().isCreated())
                .andReturn();

        String tigerId = extractId(tigerResult);

        MvcResult lionResult = mvc.perform(post("/api/v1/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(animal2)
                        .header("Idempotency-Key", "animal-lion-key"))
                .andExpect(status().isCreated())
                .andReturn();

        String lionId = extractId(lionResult);

        // Step 3: Assign favourite rooms to animals
        // Tiger loves Green and Big room
        mvc.perform(post("/api/v1/animals/{animalId}/favorites", tigerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("roomIds", List.of(greenRoomId, bigRoomId))
                        ))
                        .header("If-Match", "0"))
                .andExpect(status().isOk());

        // Lion loves only Green room
        mvc.perform(post("/api/v1/animals/{animalId}/favorites", lionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("roomIds", List.of(greenRoomId))
                        ))
                        .header("If-Match", "0"))
                .andExpect(status().isOk());

        // Step 4: Get favourite room counts
        MvcResult result = mvc.perform(get("/api/v1/rooms/favorites")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        // Step 5: Validate returned data
        String response = result.getResponse().getContentAsString();
        JsonNode jsonArray = objectMapper.readTree(response);

        assertThat(jsonArray.size()).isEqualTo(2);
        assertThat(jsonArray.get(0).get("title").asText()).isIn("Green", "Big");
        assertThat(jsonArray.toString()).contains("Green");
        assertThat(jsonArray.toString()).contains("Big");

        // Ensure "Green" room has count 2, "Big" has count 1 and "Red" is not returned
        boolean greenFound = false;
        boolean bigFound = false;
        for (JsonNode node : jsonArray) {
            if (node.get("title").asText().equals("Green")) {
                assertThat(node.get("count").asInt()).isEqualTo(2);
                greenFound = true;
            }
            if (node.get("title").asText().equals("Big")) {
                assertThat(node.get("count").asInt()).isEqualTo(1);
                bigFound = true;
            }
        }

        assertThat(greenFound).isTrue();
        assertThat(bigFound).isTrue();
    }

    private String extractId(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asText();
    }



}
