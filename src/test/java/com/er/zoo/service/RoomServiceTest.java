package com.er.zoo.service;

import static org.junit.jupiter.api.Assertions.*;

import com.er.zoo.dto.*;
import com.er.zoo.model.Room;
import com.er.zoo.repository.RoomRepository;
import com.er.zoo.logging.LoggerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private IdempotencyService idempotencyService;
    @Mock
    private LoggerService loggerService;

    @InjectMocks
    private RoomService roomService;

    private Room room;
    private RoomCreateRequest createRequest;
    private RoomUpdateRequest updateRequest;

    @BeforeEach
    void setup() {
        openMocks(this);

        room = new Room();
        room.setId("room1");
        room.setTitle("Blue");
        room.setVersion(1L);

        createRequest = new RoomCreateRequest("Blue");
        updateRequest = new RoomUpdateRequest("Jungle");
    }

    @Test
    void create_ShouldRegisterKeyAndSaveRoom() {
        when(idempotencyService.registerKey(any())).thenReturn(true);
        when(roomRepository.save(any())).thenReturn(room);

        Room result = roomService.create(createRequest, "key123");

        verify(idempotencyService).registerKey("key123");
        verify(roomRepository).save(room);
        assertEquals("Blue", result.getTitle());
    }

    @Test
    void get_ShouldReturnRoom_WhenFound() {
        when(roomRepository.findById("room1")).thenReturn(Optional.of(room));
        Room result = roomService.get("room1");
        assertEquals("Blue", result.getTitle());
    }

    @Test
    void get_ShouldThrowException_WhenNotFound() {
        when(roomRepository.findById("room1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> roomService.get("room1"));
    }

    @Test
    void update_ShouldUpdateRoomTitle() {
        when(roomRepository.findById("room1")).thenReturn(Optional.of(room));
        when(roomRepository.save(room)).thenReturn(room);

        RoomResponse result = roomService.update("room1", updateRequest, "\"1\"");

        verify(loggerService, atLeastOnce()).info(anyString(),anyString(),anyString());
        verify(roomRepository).save(room);
        assertEquals("Jungle", result.title());
    }

    @Test
    void delete_ShouldDeleteRoom_WhenIfMatchValid() {
        when(roomRepository.findById("room1")).thenReturn(Optional.of(room));
        roomService.delete("room1", "\"1\"");
        verify(roomRepository).deleteById("room1");
    }

    @Test
    void delete_ShouldThrow_WhenRoomNotFound() {
        when(roomRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> roomService.delete("missing", "\"1\""));
    }


    @Test
    @Transactional(readOnly = true)
    void favoriteRoomCounts_ShouldReturnMappedCounts() {
        RoomCountProjection projection = mock(RoomCountProjection.class);
        when(projection.title()).thenReturn("Blue");
        when(projection.favCount()).thenReturn(5L);
        when(roomRepository.findFavoriteRoomsWithCounts()).thenReturn(List.of(projection));

        List<FavoriteRoomCount> result = roomService.favoriteRoomCounts();

        assertEquals(1, result.size());
        assertEquals("Blue", result.getFirst().title());
        assertEquals(5L, result.getFirst().count());
    }
}
