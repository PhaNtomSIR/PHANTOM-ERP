package com.erp.model.manufacturing;

import com.erp.model.inventory.Product;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "bom_components")
public class BomComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id", nullable = false)
    @JsonBackReference
    private BillOfMaterial billOfMaterial;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "component_product_id", nullable = false)
    private Product componentProduct;

    @Column(nullable = false)
    private Integer requiredQuantity;

    public BomComponent() {}

    public BomComponent(Product componentProduct, Integer requiredQuantity) {
        this.componentProduct = componentProduct;
        this.requiredQuantity = requiredQuantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BillOfMaterial getBillOfMaterial() { return billOfMaterial; }
    public void setBillOfMaterial(BillOfMaterial billOfMaterial) { this.billOfMaterial = billOfMaterial; }

    public Product getComponentProduct() { return componentProduct; }
    public void setComponentProduct(Product componentProduct) { this.componentProduct = componentProduct; }

    public Integer getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(Integer requiredQuantity) { this.requiredQuantity = requiredQuantity; }
}
