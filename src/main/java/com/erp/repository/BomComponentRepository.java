package com.erp.repository;

import com.erp.model.manufacturing.BomComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BomComponentRepository extends JpaRepository<BomComponent, Long> {
}
