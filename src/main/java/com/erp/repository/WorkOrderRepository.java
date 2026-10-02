package com.erp.repository;

import com.erp.model.manufacturing.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    Optional<WorkOrder> findByWoNumber(String woNumber);
    List<WorkOrder> findByStatus(WorkOrder.WorkOrderStatus status);
}
