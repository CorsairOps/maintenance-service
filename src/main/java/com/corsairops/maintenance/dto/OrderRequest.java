package com.corsairops.maintenance.dto;

import com.corsairops.maintenance.model.OrderStatus;
import jakarta.validation.constraints.*;

public record OrderRequest(

        @NotBlank(message = "AssetId is required.")
        @Size(max = 255, message = "AssetId must be at most 255 characters.")
        String assetId,

        @NotBlank(message = "Description is required.")
        String description,

        @NotNull(message = "Order status is required.")
        OrderStatus status,

        @NotNull(message = "Priority is required.")
        @Max(value = 5, message = "Priority must be at most 5.")
        @Min(value = 1, message = "Priority must be at least 1.")
        Integer priority
) {
}