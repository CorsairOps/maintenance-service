package com.corsairops.maintenance.repository;

import com.corsairops.maintenance.model.MaintenanceOrder;
import com.corsairops.maintenance.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceOrderRepository extends JpaRepository<MaintenanceOrder, Long> {
    boolean existsByAssetIdAndStatusIn(String assetId, List<OrderStatus> orderStatusList);
}