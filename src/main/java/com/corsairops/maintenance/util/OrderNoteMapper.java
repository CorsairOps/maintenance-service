package com.corsairops.maintenance.util;

import com.corsairops.maintenance.dto.OrderNoteResponse;
import com.corsairops.maintenance.model.OrderNote;
import com.corsairops.shared.dto.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNoteMapper {

    private final UserServiceClientUtil userServiceClientUtil;

    public List<OrderNoteResponse> toResponseList(List<OrderNote> notes) {
        Set<String> userIds = extractUserIds(notes);
        Map<String, User> userMap = userServiceClientUtil.getUsersMap(userIds);
        return mapNotesToResponses(notes, userMap);
    }

    private Set<String> extractUserIds(List<OrderNote> notes) {
        return notes.stream()
                .map(OrderNote::getCreatedBy)
                .collect(Collectors.toSet());
    }

    private List<OrderNoteResponse> mapNotesToResponses(List<OrderNote> notes, Map<String, User> userMap) {
        return notes.stream()
                .map(note -> mapToResponse(note, userMap.get(note.getCreatedBy())))
                .collect(Collectors.toList());
    }

    public OrderNoteResponse toResponse(OrderNote note) {
        var createdBy = userServiceClientUtil.getUserById(note.getCreatedBy());
        log.info("Fetched user {} for note id {}", createdBy, note.getId());
        return mapToResponse(note, createdBy);
    }

    private OrderNoteResponse mapToResponse(OrderNote note, User createdBy) {
        return new OrderNoteResponse(
                note.getId(),
                note.getOrder().getId(),
                note.getNote(),
                createdBy,
                note.getCreatedAt()
        );
    }
}