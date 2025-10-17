package com.corsairops.maintenance.controller;

import com.corsairops.maintenance.dto.OrderNoteRequest;
import com.corsairops.maintenance.dto.OrderNoteResponse;
import com.corsairops.maintenance.model.OrderNote;
import com.corsairops.maintenance.service.OrderNoteService;
import com.corsairops.maintenance.util.OrderNoteMapper;
import com.corsairops.shared.annotations.CommonReadResponses;
import com.corsairops.shared.annotations.CommonWriteResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Maintenance Order Notes", description = "APIs for managing notes on maintenance orders")
@RestController
@RequestMapping("/api/maintenance/orders/{orderId}/notes")
@RequiredArgsConstructor
public class OrderNoteController {
    private final OrderNoteService orderNoteService;
    private final OrderNoteMapper orderNoteMapper;


    @Operation(summary = "Add a note to a maintenance order")
    @CommonWriteResponses
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderNoteResponse addNote(@PathVariable Long orderId,
                                     @RequestBody @Valid OrderNoteRequest orderNoteRequest,
                                     @RequestHeader("X-User-Id") String userId) {
        var note = orderNoteService.addNote(orderId, orderNoteRequest, userId);
        log.info("Added note with id {} to order with id {}", note.getId(), orderId);
        return orderNoteMapper.toResponse(note);
    }

    @Operation(summary = "Get all notes for a maintenance order")
    @CommonReadResponses
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderNoteResponse> getNotes(@PathVariable Long orderId) {
        List<OrderNote> notes = orderNoteService.getAllNotes(orderId);
        log.info("Fetched {} notes for order with id {}", notes.size(), orderId);
        return orderNoteMapper.toResponseList(notes);
    }

    @Operation(summary = "Delete a note from a maintenance order")
    @CommonWriteResponses
    @DeleteMapping("/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(@PathVariable Long orderId, @PathVariable Long noteId) {
        orderNoteService.deleteNote(orderId, noteId);
        log.info("Deleted note with id {} from order with id {}", noteId, orderId);
    }


}