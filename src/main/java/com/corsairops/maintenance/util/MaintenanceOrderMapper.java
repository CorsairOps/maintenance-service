package com.corsairops.maintenance.util;

import com.corsairops.maintenance.dto.MaintenanceOrderResponse;
import com.corsairops.maintenance.model.MaintenanceOrder;
import com.corsairops.shared.client.AssetServiceClient;
import com.corsairops.shared.client.UserServiceClient;
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
public class MaintenanceOrderMapper {
    private final AssetServiceClient assetServiceClient;
    private final UserServiceClient userServiceClient;

    public MaintenanceOrderResponse toResponse(MaintenanceOrder order) {
        AssetResponse asset = getAssetById(order.getAssetId());
        Map<String, User> users = getRelevantUsers(order);
        User placedBy = users.get(order.getPlacedBy());
        User completedBy = order.getCompletedBy() != null ? users.get(order.getCompletedBy()) : null;
        return new MaintenanceOrderResponse(
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


    public Map<String, User> getRelevantUsers(MaintenanceOrder order) {
        try {
            StringBuilder userIds = new StringBuilder();
            userIds.append(order.getPlacedBy());
            if (order.getCompletedBy() != null) {
                userIds.append(",").append(order.getCompletedBy());
            }

            return userServiceClient.getUsersByIds(userIds.toString(), true)
                    .stream()
                    .collect(Collectors.toMap(User::id, user -> user));
        } catch(HttpClientErrorException e) {
            log.error("Error fetching users for order {}: {}", order.getId(), e.getMessage());
            Map<String, User> users = new HashMap<>();
            users.put(order.getPlacedBy(), new User(order.getPlacedBy(), null, null, null, null, true, null, null));
            if (order.getCompletedBy() != null) {
                users.put(order.getCompletedBy(), new User(order.getCompletedBy(), null, null, null, null, true, null, null));
            }
            return users;
        }

    }

    public User getUserById(String userId) {
        try {
            return userServiceClient.getUserById(userId);
        } catch (HttpClientErrorException e) {
            log.error("Error fetching user {}: {}", userId, e.getMessage());
            return new User(userId, null, null, null, null, true, null, null);
        }
    }

    public AssetResponse getAssetById(String assetId) {
        try {
            return assetServiceClient.getAssetById(UUID.fromString(assetId));
        } catch (HttpClientErrorException e) {
            log.error("Error fetching asset {}: {}", assetId, e.getMessage());
            return new AssetResponse(UUID.fromString(assetId), null, null, null, null, null, null, null);
        }
    }
}