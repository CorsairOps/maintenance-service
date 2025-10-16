package com.corsairops.maintenance.controller;

import com.corsairops.maintenance.dto.MaintenanceOrderRequest;
import com.corsairops.maintenance.dto.MaintenanceOrderResponse;
import com.corsairops.maintenance.model.MaintenanceOrder;
import com.corsairops.maintenance.service.MaintenanceOrderService;
import com.corsairops.maintenance.util.MaintenanceOrderMapper;
import com.corsairops.shared.annotations.CommonReadResponses;
import com.corsairops.shared.annotations.CommonWriteResponses;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                                                @RequestHeader(value = "X-User-Id") String userId) {
        MaintenanceOrder order = maintenanceOrderService.createOrder(maintenanceOrderRequest, userId);
        return maintenanceOrderMapper.toResponse(order);
    }

    @Operation(summary = "Get a list of all maintenance orders")
    @CommonReadResponses
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MaintenanceOrderResponse> getAllOrders() {
        List<MaintenanceOrder> orders = maintenanceOrderService.getAllOrders();
        return maintenanceOrderMapper.toResponseList(orders);
    }

    @Operation(summary = "Get a maintenance order by ID")
    @CommonReadResponses
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public MaintenanceOrderResponse getOrderById(@PathVariable Long id) {
        MaintenanceOrder order = maintenanceOrderService.getOrderById(id);
        return maintenanceOrderMapper.toResponse(order);
    }

    @Operation(summary = "Update a maintenance order by ID")
    @CommonWriteResponses
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public MaintenanceOrderResponse updateOrder(@PathVariable Long id,
                                                @RequestBody @Valid MaintenanceOrderRequest maintenanceOrderRequest) {
        MaintenanceOrder order = maintenanceOrderService.updateOrder(id, maintenanceOrderRequest);
        return maintenanceOrderMapper.toResponse(order);
    }

    @Operation(summary = "Delete a maintenance order by ID")
    @CommonWriteResponses
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {
        maintenanceOrderService.deleteOrder(id);
    }
}