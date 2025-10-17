package com.corsairops.maintenance.dto;

import jakarta.validation.constraints.NotBlank;

public record OrderNoteRequest(
        @NotBlank(message = "Note cannot be blank")
        String note
) {
}