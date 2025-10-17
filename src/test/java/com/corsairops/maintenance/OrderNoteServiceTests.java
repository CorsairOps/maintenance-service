package com.corsairops.maintenance;


import com.corsairops.maintenance.dto.OrderNoteRequest;
import com.corsairops.maintenance.exception.OrderNotFoundException;
import com.corsairops.maintenance.model.Order;
import com.corsairops.maintenance.model.OrderNote;
import com.corsairops.maintenance.model.OrderStatus;
import com.corsairops.maintenance.repository.OrderRepository;
import com.corsairops.maintenance.repository.OrderNoteRepository;
import com.corsairops.maintenance.service.OrderNoteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
public class OrderNoteServiceTests {

    @Autowired
    private OrderNoteRepository orderNoteRepository;

    @Autowired
    private OrderNoteService orderNoteService;

    @Autowired
    private OrderRepository orderRepository;

    private Long validOrderId;

    @BeforeEach
    void setup() {
        // Create valid maintenance order and get its ID
        Order order = Order.builder()
                .assetId("123e4567-e89b-12d3-a456-426614174000")
                .description("Routine check")
                .status(OrderStatus.PENDING)
                .priority(1)
                .placedBy("tech1")
                .build();

        var savedOrder = orderRepository.save(order);
        validOrderId = savedOrder.getId();
    }

    @AfterEach
    void cleanup() {
        orderRepository.deleteAll();
        orderNoteRepository.deleteAll();
    }

    @Test
    void givenInvalidOrderId_whenAddNote_thenThrowOrderNotFoundException() {
        Long invalidOrderId = 999L;
        OrderNoteRequest orderNoteRequest = new OrderNoteRequest("This is a test note");

        assertThrows(OrderNotFoundException.class, () ->
                orderNoteService.addNote(invalidOrderId, orderNoteRequest, "tech1"));
    }

    @Test
    void givenValidOrderIdAndNoteRequest_whenAddNote_thenReturnOrderNote() {
        OrderNoteRequest orderNoteRequest = new OrderNoteRequest("This is a test note");

        OrderNote newNote = orderNoteService.addNote(validOrderId, orderNoteRequest, "tech1");
        assertThat(newNote, notNullValue());
        assertThat(newNote.getId(), notNullValue());
        assertThat(newNote.getOrder().getId(), equalTo(validOrderId));
        assertThat(newNote.getNote(), equalTo("This is a test note"));
        assertThat(newNote.getCreatedBy(), equalTo("tech1"));
        assertThat(newNote.getCreatedAt(), notNullValue());
    }

    @Test
    void givenValidOrderId_whenGetAllNotes_thenReturnNotesList() {
        orderNoteService.addNote(validOrderId,  new OrderNoteRequest("First note"), "tech1");
        orderNoteService.addNote(validOrderId, new OrderNoteRequest("Second note"), "tech2");

        List<OrderNote> notes = orderNoteService.getAllNotes(validOrderId);
        assertThat(notes, hasSize(2));
        assertThat(notes, hasItem(hasProperty("note", equalTo("First note"))));
        assertThat(notes, hasItem(hasProperty("note", equalTo("Second note"))));

        assertTrue(notes.getFirst().getCreatedAt().isAfter(notes.getLast().getCreatedAt()));
    }

    @Test
    void givenInvalidOrderId_whenGetAllNotes_thenThrowOrderNotFoundException() {
        Long invalidOrderId = 999L;

        assertThrows(OrderNotFoundException.class, () ->
                orderNoteService.getAllNotes(invalidOrderId));
    }

    @Test
    void givenNoNotes_whenGetAllNotes_thenReturnEmptyList() {
        List<OrderNote> notes = orderNoteService.getAllNotes(validOrderId);
        assertThat(notes, hasSize(0));
    }

    @Test
    void givenInvalidOrderId_whenDeleteNote_thenThrowOrderNotFoundException() {
        Long invalidOrderId = 999L;
        Long noteId = 1L;

        assertThrows(OrderNotFoundException.class, () ->
                orderNoteService.deleteNote(invalidOrderId, noteId));
    }

    @Test
    void givenValidOrderIdAndInvalidNoteId_whenDeleteNote_thenThrowOrderNoteNotFoundException() {
        Long invalidNoteId = 999L;

        assertThrows(com.corsairops.maintenance.exception.OrderNoteNotFoundException.class, () ->
                orderNoteService.deleteNote(validOrderId, invalidNoteId));
    }

    @Test
    void givenValidOrderIdAndNoteId_whenDeleteNote_thenNoteIsDelete() {
        OrderNote newNote = orderNoteService.addNote(validOrderId, new OrderNoteRequest("Note to be deleted"), "tech1");
        Long noteId = newNote.getId();

        orderNoteService.deleteNote(validOrderId, noteId);

        List<OrderNote> notes = orderNoteService.getAllNotes(validOrderId);
        assertThat(notes, hasSize(0));
    }

}