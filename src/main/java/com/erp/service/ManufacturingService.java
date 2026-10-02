package com.erp.service;

import com.erp.model.audit.AuditLog;
import com.erp.model.finance.Account;
import com.erp.model.finance.JournalEntry;
import com.erp.model.finance.JournalLine;
import com.erp.model.inventory.Product;
import com.erp.model.inventory.StockMovement;
import com.erp.model.manufacturing.BillOfMaterial;
import com.erp.model.manufacturing.BomComponent;
import com.erp.model.manufacturing.WorkOrder;
import com.erp.repository.*;
import com.erp.repository.*;
import com.erp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ManufacturingService {

    private final BillOfMaterialRepository bomRepo;
    private final WorkOrderRepository woRepo;
    private final InventoryService inventoryService;
    private final FinanceService financeService;
    private final AuditLogRepository auditRepo;

    public ManufacturingService(BillOfMaterialRepository bomRepo,
                                WorkOrderRepository woRepo,
                                InventoryService inventoryService,
                                FinanceService financeService,
                                AuditLogRepository auditRepo) {
        this.bomRepo = bomRepo;
        this.woRepo = woRepo;
        this.inventoryService = inventoryService;
        this.financeService = financeService;
        this.auditRepo = auditRepo;
    }

    public List<BillOfMaterial> getAllBoms() {
        return bomRepo.findAll();
    }

    public BillOfMaterial saveBom(BillOfMaterial bom) {
        if (bom.getComponents() != null) {
            bom.getComponents().forEach(c -> c.setBillOfMaterial(bom));
        }
        BillOfMaterial saved = bomRepo.save(bom);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.MANUFACTURING, "CREATE_BOM",
                saved.getBomNumber(), "Production Engineer",
                "Created BOM for " + (saved.getFinishedProduct() != null ? saved.getFinishedProduct().getName() : "N/A")));
        return saved;
    }

    public List<WorkOrder> getAllWorkOrders() {
        return woRepo.findAll();
    }

    public WorkOrder getWorkOrderById(Long id) {
        return woRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work Order not found: " + id));
    }

    public WorkOrder saveWorkOrder(WorkOrder wo) {
        WorkOrder saved = woRepo.save(wo);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.MANUFACTURING, "CREATE_WORK_ORDER",
                saved.getWoNumber(), "Plant Manager",
                "Created Work Order for " + saved.getTargetQuantity() + " units"));
        return saved;
    }

    public WorkOrder startWorkOrder(Long woId) {
        WorkOrder wo = getWorkOrderById(woId);
        wo.setStatus(WorkOrder.WorkOrderStatus.IN_PROGRESS);
        WorkOrder saved = woRepo.save(wo);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.MANUFACTURING, "START_PRODUCTION",
                saved.getWoNumber(), "Plant Floor Supervisor", "Production run initiated."));
        return saved;
    }

    @Transactional
    public WorkOrder completeWorkOrder(Long woId, Integer completedQty) {
        WorkOrder wo = getWorkOrderById(woId);
        if (wo.getStatus() == WorkOrder.WorkOrderStatus.COMPLETED) {
            throw new IllegalStateException("Work Order " + wo.getWoNumber() + " is already completed.");
        }

        int qtyToProduce = completedQty != null && completedQty > 0 ? completedQty : wo.getTargetQuantity();
        BillOfMaterial bom = wo.getBillOfMaterial();
        BigDecimal totalMaterialCost = BigDecimal.ZERO;

        // 1. Consume raw materials based on BOM recipe
        for (BomComponent component : bom.getComponents()) {
            int neededQuantity = component.getRequiredQuantity() * qtyToProduce;
            inventoryService.recordMovement(
                    component.getComponentProduct().getId(),
                    StockMovement.MovementType.PRODUCTION_CONSUMPTION,
                    -neededQuantity,
                    wo.getWoNumber(),
                    "Consumed in assembly for WO " + wo.getWoNumber()
            );

            BigDecimal compCost = component.getComponentProduct().getUnitCost() != null ?
                    component.getComponentProduct().getUnitCost().multiply(BigDecimal.valueOf(neededQuantity)) : BigDecimal.ZERO;
            totalMaterialCost = totalMaterialCost.add(compCost);
        }

        // 2. Produce Finished Goods into Inventory
        Product finishedGood = bom.getFinishedProduct();
        inventoryService.recordMovement(
                finishedGood.getId(),
                StockMovement.MovementType.PRODUCTION_OUTPUT,
                qtyToProduce,
                wo.getWoNumber(),
                "Manufactured batch output from WO " + wo.getWoNumber()
        );

        // 3. Labor and overhead calculation
        BigDecimal laborCost = bom.getLaborCost() != null ?
                bom.getLaborCost().multiply(BigDecimal.valueOf(qtyToProduce)) : BigDecimal.ZERO;
        BigDecimal totalMfgCost = totalMaterialCost.add(laborCost);

        // 4. Post Journal Entry to General Ledger:
        // Debit Finished Goods Inventory (1030)
        // Credit Raw Materials Inventory & Labor Expenses
        Account invAcc = financeService.getAccountByCode("1030");
        Account laborExpAcc = financeService.getAccountByCode("5020");

        JournalEntry je = new JournalEntry(
                "JE-WO-" + wo.getWoNumber(),
                LocalDate.now(),
                "Manufacturing Completion for " + wo.getWoNumber(),
                "MANUFACTURING",
                wo.getWoNumber()
        );
        je.addLine(new JournalLine(invAcc, totalMfgCost, BigDecimal.ZERO, "Finished Goods Added to Inventory"));
        if (totalMaterialCost.compareTo(BigDecimal.ZERO) > 0) {
            je.addLine(new JournalLine(invAcc, BigDecimal.ZERO, totalMaterialCost, "Raw Material Inventory Consumed"));
        }
        if (laborCost.compareTo(BigDecimal.ZERO) > 0) {
            je.addLine(new JournalLine(laborExpAcc, BigDecimal.ZERO, laborCost, "Factory Labor Allocated"));
        }
        financeService.createJournalEntry(je);

        // 5. Update Work Order
        wo.setCompletedQuantity(qtyToProduce);
        wo.setCompletionDate(LocalDate.now());
        wo.setStatus(WorkOrder.WorkOrderStatus.COMPLETED);
        WorkOrder updatedWo = woRepo.save(wo);

        auditRepo.save(new AuditLog(AuditLog.ModuleName.MANUFACTURING, "COMPLETE_WORK_ORDER",
                wo.getWoNumber(), "Plant Floor Supervisor",
                "Successfully manufactured " + qtyToProduce + " units of " + finishedGood.getName()
                        + ". Inventory updated & GL entries posted."));

        return updatedWo;
    }
}
