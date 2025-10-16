package com.corsairops.maintenance.controller;

import com.corsairops.maintenance.dto.MaintenanceOrderRequest;
import com.corsairops.maintenance.dto.MaintenanceOrderResponse;
import com.corsairops.maintenance.model.MaintenanceOrder;
import com.corsairops.maintenance.service.MaintenanceOrderService;
import com.corsairops.maintenance.util.MaintenanceOrderMapper;
import com.corsairops.shared.annotations.CommonWriteResponses;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maintenance/orders")
@RequiredArgsConstructor
public class MaintenanceOrderController {

    private final MaintenanceOrderService maintenanceOrderService;
    private final MaintenanceOrderMapper maintenanceOrderMapper;

    @Operation(summary = "Create a new maintenance order")
    @CommonWriteResponses
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceOrderResponse createOrder(@RequestBody @Valid MaintenanceOrderRequest maintenanceOrderRequest,
                                                @RequestHeader(value = "X-User-Id", required = false) String userId) {
        MaintenanceOrder order = maintenanceOrderService.createOrder(maintenanceOrderRequest, userId);
        return maintenanceOrderMapper.toResponse(order);
    }
}