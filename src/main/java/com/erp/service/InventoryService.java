package com.erp.service;

import com.erp.model.audit.AuditLog;
import com.erp.model.inventory.Product;
import com.erp.model.inventory.StockMovement;
import com.erp.repository.*;
import com.erp.repository.*;
import com.erp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventoryService {

    private final ProductRepository productRepo;
    private final StockMovementRepository movementRepo;
    private final AuditLogRepository auditRepo;

    public InventoryService(ProductRepository productRepo,
                            StockMovementRepository movementRepo,
                            AuditLogRepository auditRepo) {
        this.productRepo = productRepo;
        this.movementRepo = movementRepo;
        this.auditRepo = auditRepo;
    }

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public Product getProductById(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    public Product saveProduct(Product product) {
        boolean isNew = product.getId() == null;
        Product saved = productRepo.save(product);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.INVENTORY,
                isNew ? "CREATE_PRODUCT" : "UPDATE_PRODUCT",
                saved.getSku(), "Inventory Manager", "Saved product: " + saved.getName()));
        return saved;
    }

    public List<Product> getLowStockAlerts() {
        return productRepo.findLowStockProducts();
    }

    public List<StockMovement> getRecentMovements() {
        return movementRepo.findTop50ByOrderByTimestampDesc();
    }

    public BigDecimal getTotalInventoryValuation() {
        return productRepo.findAll().stream()
                .map(Product::getTotalValuation)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public StockMovement recordMovement(Long productId, StockMovement.MovementType type,
                                         int quantity, String referenceNumber, String notes) {
        Product product = getProductById(productId);
        int updatedStock = (product.getCurrentStock() != null ? product.getCurrentStock() : 0) + quantity;
        if (updatedStock < 0) {
            throw new IllegalStateException("Insufficient stock for product " + product.getSku()
                    + " (" + product.getName() + "). Current: " + product.getCurrentStock() + ", Requested change: " + quantity);
        }
        product.setCurrentStock(updatedStock);
        productRepo.save(product);

        StockMovement movement = new StockMovement(product, type, quantity, updatedStock, referenceNumber, notes);
        StockMovement savedMovement = movementRepo.save(movement);

        auditRepo.save(new AuditLog(AuditLog.ModuleName.INVENTORY, "STOCK_UPDATE",
                product.getSku(), "System / Inventory",
                type.name() + " Qty: " + quantity + " Ref: " + referenceNumber + ", Balance: " + updatedStock));

        return savedMovement;
    }
}
