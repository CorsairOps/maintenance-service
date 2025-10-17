package com.corsairops.maintenance.controller;

import com.corsairops.maintenance.dto.OrderRequest;
import com.corsairops.maintenance.dto.OrderResponse;
import com.corsairops.maintenance.model.Order;
import com.corsairops.maintenance.service.OrderService;
import com.corsairops.maintenance.util.OrderMapper;
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
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @Operation(summary = "Create a new maintenance order")
    @CommonWriteResponses
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestBody @Valid OrderRequest orderRequest,
                                     @RequestHeader(value = "X-User-Id") String userId) {
        Order order = orderService.createOrder(orderRequest, userId);
        return orderMapper.toResponse(order);
    }

    @Operation(summary = "Get a list of all maintenance orders")
    @CommonReadResponses
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return orderMapper.toResponseList(orders);
    }

    @Operation(summary = "Get a maintenance order by ID")
    @CommonReadResponses
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return orderMapper.toResponse(order);
    }

    @Operation(summary = "Update a maintenance order by ID")
    @CommonWriteResponses
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse updateOrder(@PathVariable Long id,
                                     @RequestBody @Valid OrderRequest orderRequest) {
        Order order = orderService.updateOrder(id, orderRequest);
        return orderMapper.toResponse(order);
    }

    @Operation(summary = "Delete a maintenance order by ID")
    @CommonWriteResponses
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }
}