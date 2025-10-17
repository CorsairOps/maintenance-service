package com.corsairops.maintenance.util;

import com.corsairops.maintenance.dto.OrderResponse;
import com.corsairops.maintenance.model.Order;
import com.corsairops.shared.client.AssetServiceClient;
import com.corsairops.shared.dto.User;
import com.corsairops.shared.dto.asset.AssetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMapper {
    private final AssetServiceClient assetServiceClient;
    private final UserServiceClientUtil userServiceClientUtil;

    /**
     * Convert a MaintenanceOrder entity to a MaintenanceOrderResponse DTO.
     *
     * @param order the MaintenanceOrder entity
     * @return the MaintenanceOrderResponse DTO
     */
    public OrderResponse toResponse(Order order) {
        AssetResponse asset = getAssetById(order.getAssetId());
        Map<String, User> users = getRelevantUsers(order);
        User placedBy = users.get(order.getPlacedBy());
        User completedBy = order.getCompletedBy() != null ? users.get(order.getCompletedBy()) : null;
        return createResponse(order, asset, placedBy, completedBy);
    }

    private Map<String, User> getRelevantUsers(Order order) {
        Set<String> userIds = new HashSet<>();
        if (order.getPlacedBy() != null) {
            userIds.add(order.getPlacedBy());
        }
        if (order.getCompletedBy() != null) {
            userIds.add(order.getCompletedBy());
        }
        return userServiceClientUtil.getUsersMap(userIds);
    }

    private AssetResponse getAssetById(String assetId) {
        try {
            return assetServiceClient.getAssetById(UUID.fromString(assetId));
        } catch (HttpClientErrorException e) {
            log.error("Error fetching asset {}: {}", assetId, e.getMessage());
            return new AssetResponse(UUID.fromString(assetId), null, null, null, null, null, null, null);
        }
    }

    /**
     * Convert a list of MaintenanceOrder entities to a list of MaintenanceOrderResponse DTOs.
     *
     * @param orders the list of MaintenanceOrder entities
     * @return the list of MaintenanceOrderResponse DTOs
     */
    public List<OrderResponse> toResponseList(List<Order> orders) {
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, AssetResponse> assets = getRelevantAssets(orders);
        Map<String, User> users = getRelevantUsers(orders);

        List<OrderResponse> responses = new ArrayList<>();
        for (Order order : orders) {
            AssetResponse asset = assets.get(order.getAssetId());
            User placedBy = users.get(order.getPlacedBy());
            User completedBy = order.getCompletedBy() != null ? users.get(order.getCompletedBy()) : null;
            responses.add(createResponse(order, asset, placedBy, completedBy));
        }
        return responses;
    }

    private Map<String, User> getRelevantUsers(List<Order> orders) {
        Set<String> userIds = new HashSet<>();
        orders.forEach(order -> {
            if (order.getPlacedBy() != null) {
                userIds.add(order.getPlacedBy());
            }
            if (order.getCompletedBy() != null) {
                userIds.add(order.getCompletedBy());
            }
        });
        return userServiceClientUtil.getUsersMap(userIds);
    }

    private Map<String, AssetResponse> getRelevantAssets(List<Order> orders) {
        try {
            Set<String> assetIds = orders.stream()
                    .map(Order::getAssetId)
                    .collect(Collectors.toSet());
            Map<String, AssetResponse> assets = new HashMap<>();
            for (String assetId : assetIds) {
                AssetResponse asset = assetServiceClient.getAssetById(UUID.fromString(assetId));
                assets.put(assetId, asset);
            }
            return assets;
        } catch (HttpClientErrorException e) {
            log.error("Error fetching assets for orders: {}", e.getMessage());
            Map<String, AssetResponse> assets = new HashMap<>();
            orders.forEach(order -> {
                String assetId = order.getAssetId();
                assets.put(assetId, new AssetResponse(UUID.fromString(assetId), null, null, null, null, null, null, null));
            });
            return assets;
        }
    }

    private static OrderResponse createResponse(Order order, AssetResponse asset, User placedBy, User completedBy) {
        return new OrderResponse(
                order.getId(),
                asset,
                order.getDescription(),
                order.getStatus(),
                order.getPriority(),
                placedBy,
                completedBy,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}