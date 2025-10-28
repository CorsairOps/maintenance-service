package com.corsairops.maintenance.service;

import com.corsairops.maintenance.dto.OrderRequest;
import com.corsairops.maintenance.exception.OpenOrderExistsException;
import com.corsairops.maintenance.exception.OrderNotFoundException;
import com.corsairops.maintenance.model.Order;
import com.corsairops.maintenance.model.OrderStatus;
import com.corsairops.maintenance.repository.OrderRepository;
import com.corsairops.shared.client.AssetServiceClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final AssetServiceClient assetServiceClient;

    @Transactional
    public Order createOrder(@Valid OrderRequest request, String placedBy) {
        // Validate asset existence
        assetServiceClient.getAssetById(UUID.fromString(request.assetId()));

        // Check if asset already has a PENDING or IN_PROGRESS order.
        if (orderRepository.existsByAssetIdAndStatusIn(request.assetId(), List.of(OrderStatus.PENDING, OrderStatus.IN_PROGRESS))) {
            throw new OpenOrderExistsException("An open order already exists for asset " + request.assetId(), HttpStatus.CONFLICT);
        }

        Order order = Order.builder()
                .assetId(request.assetId())
                .description(request.description())
                .status(request.status())
                .priority(request.priority())
                .placedBy(placedBy)
                .build();

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders(String assetId) {
        return orderRepository.findByAssetId(assetId);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId + " not found.", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Order updateOrder(Long orderId, OrderRequest request) {
        Order existingOrder = getOrderById(orderId);
        existingOrder.setDescription(request.description());
        existingOrder.setStatus(request.status());
        existingOrder.setPriority(request.priority());
        return orderRepository.save(existingOrder);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new OrderNotFoundException("Order with ID " + orderId + " not found.", HttpStatus.NOT_FOUND);
        }
        orderRepository.deleteById(orderId);
    }
}