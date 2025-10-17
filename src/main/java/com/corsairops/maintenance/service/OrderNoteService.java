package com.corsairops.maintenance.service;

import com.corsairops.maintenance.dto.OrderNoteRequest;
import com.corsairops.maintenance.exception.OrderNoteNotFoundException;
import com.corsairops.maintenance.model.OrderNote;
import com.corsairops.maintenance.repository.OrderNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderNoteService {
    private final OrderNoteRepository orderNoteRepository;
    private final OrderService orderService;

    @Transactional
    public OrderNote addNote(Long orderId, OrderNoteRequest request, String createdBy) {
        var order = orderService.getOrderById(orderId);

        var note = OrderNote.builder()
                .order(order)
                .note(request.note())
                .createdBy(createdBy)
                .build();
        return orderNoteRepository.save(note);
    }

    @Transactional(readOnly = true)
    public List<OrderNote> getAllNotes(Long orderId) {
        var order = orderService.getOrderById(orderId);
        return orderNoteRepository.findByOrderOrderByCreatedAtDesc(order);
    }

    @Transactional
    public void deleteNote(Long orderId, Long noteId) {
        var order = orderService.getOrderById(orderId);
        if (!orderNoteRepository.existsByIdAndOrder(noteId, order)) {
            throw new OrderNoteNotFoundException(String.format("Order with id %d does not have a note with id %d", orderId, noteId), HttpStatus.NOT_FOUND);
        }
        orderNoteRepository.deleteById(noteId);
    }
}