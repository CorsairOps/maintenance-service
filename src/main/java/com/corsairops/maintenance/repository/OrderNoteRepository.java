package com.corsairops.maintenance.repository;

import com.corsairops.maintenance.model.Order;
import com.corsairops.maintenance.model.OrderNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderNoteRepository extends JpaRepository<OrderNote, Long> {
    List<OrderNote> findByOrderOrderByCreatedAtDesc(Order order);

    boolean existsByIdAndOrder(Long attr0, Order order);
}