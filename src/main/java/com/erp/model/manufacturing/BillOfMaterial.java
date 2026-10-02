package com.erp.model.manufacturing;

import com.erp.model.inventory.Product;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bills_of_materials")
public class BillOfMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bomNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "finished_product_id", nullable = false)
    private Product finishedProduct;

    private String description;
    private BigDecimal laborCost = BigDecimal.ZERO;
    private Double standardProductionHours = 1.0;

    @OneToMany(mappedBy = "billOfMaterial", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<BomComponent> components = new ArrayList<>();

    public BillOfMaterial() {}

    public BillOfMaterial(String bomNumber, Product finishedProduct, String description,
                          BigDecimal laborCost, Double standardProductionHours) {
        this.bomNumber = bomNumber;
        this.finishedProduct = finishedProduct;
        this.description = description;
        this.laborCost = laborCost;
        this.standardProductionHours = standardProductionHours;
    }

    public void addComponent(BomComponent component) {
        components.add(component);
        component.setBillOfMaterial(this);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBomNumber() { return bomNumber; }
    public void setBomNumber(String bomNumber) { this.bomNumber = bomNumber; }

    public Product getFinishedProduct() { return finishedProduct; }
    public void setFinishedProduct(Product finishedProduct) { this.finishedProduct = finishedProduct; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getLaborCost() { return laborCost; }
    public void setLaborCost(BigDecimal laborCost) { this.laborCost = laborCost; }

    public Double getStandardProductionHours() { return standardProductionHours; }
    public void setStandardProductionHours(Double standardProductionHours) { this.standardProductionHours = standardProductionHours; }

    public List<BomComponent> getComponents() { return components; }
    public void setComponents(List<BomComponent> components) { this.components = components; }
}
