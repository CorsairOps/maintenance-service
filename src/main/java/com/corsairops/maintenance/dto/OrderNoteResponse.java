package com.corsairops.maintenance.dto;

import com.corsairops.shared.dto.User;

import java.time.LocalDateTime;

public record OrderNoteResponse(
    Long id,
    Long orderId,
    String note,
    User createdBy,
    LocalDateTime createdAt
) {
}