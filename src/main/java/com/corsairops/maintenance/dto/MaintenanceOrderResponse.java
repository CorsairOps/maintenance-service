package com.corsairops.maintenance.dto;

import com.corsairops.maintenance.model.OrderStatus;
import com.corsairops.shared.dto.User;
import com.corsairops.shared.dto.asset.AssetResponse;

import java.time.LocalDateTime;

public record MaintenanceOrderResponse(
        Long id,
        AssetResponse asset,
        String description,
        OrderStatus status,
        Integer priority,
        User placedBy,
        User completedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}