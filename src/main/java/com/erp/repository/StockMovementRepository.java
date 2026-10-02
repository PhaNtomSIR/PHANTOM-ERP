package com.erp.repository;

import com.erp.model.inventory.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findTop50ByOrderByTimestampDesc();
    List<StockMovement> findByProductIdOrderByTimestampDesc(Long productId);
}
