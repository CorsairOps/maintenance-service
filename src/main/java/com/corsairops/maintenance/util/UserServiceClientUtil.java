package com.corsairops.maintenance.util;

import com.corsairops.shared.client.UserServiceClient;
import com.corsairops.shared.dto.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClientUtil {
    private final UserServiceClient userServiceClient;

    public User getUserById(String id) {
        try {
            log.info("Fetching user with id {} from User Service", id);
            if (id == null || id.isEmpty()) {
                return null;
            }
            return userServiceClient.getUserById(id);
        } catch (HttpClientErrorException e) {
            log.error("Error fetching user with id {} from User Service: {}", id, e.getMessage());
            return getUserPlaceholder(id);
        }
    }

    public Map<String, User> getUsersMap(Set<String> ids) {
        try {
            if (ids.isEmpty()) {
                return Map.of();
            }

            String idsParam = String.join(",", ids);
            Map<String, User> users = userServiceClient.getUsersByIds(idsParam, true)
                    .stream()
                    .collect(Collectors.toMap(User::id, user -> user));
            ids.forEach(id -> users.putIfAbsent(id, getUserPlaceholder(id)));

            return users;
        } catch (HttpClientErrorException e) {
            log.error("Error fetching users from User Service: {}", e.getMessage());
            return ids.stream().collect(Collectors.toMap(id -> id, this::getUserPlaceholder));
        }
    }

    private User getUserPlaceholder(String userId) {
        return new User(userId, null, null, null, null, true, null, List.of());
    }
}