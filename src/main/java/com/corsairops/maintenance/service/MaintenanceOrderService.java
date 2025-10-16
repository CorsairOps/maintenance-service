package com.corsairops.maintenance.service;

import com.corsairops.maintenance.dto.MaintenanceOrderRequest;
import com.corsairops.maintenance.exception.OpenOrderExistsException;
import com.corsairops.maintenance.exception.OrderNotFoundException;
import com.corsairops.maintenance.model.MaintenanceOrder;
import com.corsairops.maintenance.model.OrderStatus;
import com.corsairops.maintenance.repository.MaintenanceOrderRepository;
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
public class MaintenanceOrderService {
    private final MaintenanceOrderRepository maintenanceOrderRepository;
    private final AssetServiceClient assetServiceClient;

    @Transactional
    public MaintenanceOrder createOrder(@Valid MaintenanceOrderRequest request, String placedBy) {
        // Validate asset existence
        assetServiceClient.getAssetById(UUID.fromString(request.assetId()));

        // Check if asset already has a PENDING or IN_PROGRESS order.
        if (maintenanceOrderRepository.existsByAssetIdAndStatusIn(request.assetId(), List.of(OrderStatus.PENDING, OrderStatus.IN_PROGRESS))) {
            throw new OpenOrderExistsException("An open order already exists for asset " + request.assetId(), HttpStatus.CONFLICT);
        }

        MaintenanceOrder order = MaintenanceOrder.builder()
                .assetId(request.assetId())
                .description(request.description())
                .status(request.status())
                .priority(request.priority())
                .placedBy(placedBy)
                .build();

        return maintenanceOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceOrder> getAllOrders() {
        return maintenanceOrderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MaintenanceOrder getOrderById(Long orderId) {
        return maintenanceOrderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId + " not found.", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public MaintenanceOrder updateOrder(Long orderId, MaintenanceOrderRequest request) {
        MaintenanceOrder existingOrder = getOrderById(orderId);
        existingOrder.setDescription(request.description());
        existingOrder.setStatus(request.status());
        existingOrder.setPriority(request.priority());
        return maintenanceOrderRepository.save(existingOrder);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        if (!maintenanceOrderRepository.existsById(orderId)) {
            throw new OrderNotFoundException("Order with ID " + orderId + " not found.", HttpStatus.NOT_FOUND);
        }
        maintenanceOrderRepository.deleteById(orderId);
    }
}