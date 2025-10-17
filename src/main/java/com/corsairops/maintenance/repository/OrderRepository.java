package com.corsairops.maintenance.repository;

import com.corsairops.maintenance.model.Order;
import com.corsairops.maintenance.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByAssetIdAndStatusIn(String assetId, List<OrderStatus> orderStatusList);
}