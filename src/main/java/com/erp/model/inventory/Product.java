package com.erp.model.inventory;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category; // RAW_MATERIAL, FINISHED_GOOD, COMPONENT

    private String unitOfMeasure; // PCS, KG, LITERS, BOX
    private BigDecimal unitCost;
    private BigDecimal sellingPrice;
    private Integer currentStock;
    private Integer minStockLevel;
    private String warehouseLocation;

    public enum ProductCategory {
        RAW_MATERIAL, FINISHED_GOOD, COMPONENT
    }

    public Product() {}

    public Product(String sku, String name, String description, ProductCategory category,
                   String unitOfMeasure, BigDecimal unitCost, BigDecimal sellingPrice,
                   Integer currentStock, Integer minStockLevel, String warehouseLocation) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.category = category;
        this.unitOfMeasure = unitOfMeasure;
        this.unitCost = unitCost;
        this.sellingPrice = sellingPrice;
        this.currentStock = currentStock;
        this.minStockLevel = minStockLevel;
        this.warehouseLocation = warehouseLocation;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ProductCategory getCategory() { return category; }
    public void setCategory(ProductCategory category) { this.category = category; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

    public Integer getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(Integer minStockLevel) { this.minStockLevel = minStockLevel; }

    public String getWarehouseLocation() { return warehouseLocation; }
    public void setWarehouseLocation(String warehouseLocation) { this.warehouseLocation = warehouseLocation; }

    public boolean isLowStock() {
        return currentStock != null && minStockLevel != null && currentStock <= minStockLevel;
    }

    public BigDecimal getTotalValuation() {
        if (currentStock == null || unitCost == null) return BigDecimal.ZERO;
        return unitCost.multiply(BigDecimal.valueOf(currentStock));
    }
}
