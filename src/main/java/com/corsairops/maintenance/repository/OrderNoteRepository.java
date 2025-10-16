package com.corsairops.maintenance.repository;

import com.corsairops.maintenance.model.MaintenanceOrder;
import com.corsairops.maintenance.model.OrderNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderNoteRepository extends JpaRepository<OrderNote, Long> {
    OrderNote findByOrder(MaintenanceOrder order);

    List<OrderNote> findByOrderOrderByCreatedAtDesc(MaintenanceOrder order);

    boolean existsByIdAndOrder(Long attr0, MaintenanceOrder order);
}