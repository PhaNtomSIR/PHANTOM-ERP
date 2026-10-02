package com.erp.repository;

import com.erp.model.manufacturing.BillOfMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillOfMaterialRepository extends JpaRepository<BillOfMaterial, Long> {
    Optional<BillOfMaterial> findByBomNumber(String bomNumber);
}
