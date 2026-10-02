package com.erp.service;

import com.erp.model.audit.AuditLog;
import com.erp.model.finance.Account;
import com.erp.model.finance.Invoice;
import com.erp.model.finance.JournalEntry;
import com.erp.model.finance.JournalLine;
import com.erp.model.inventory.StockMovement;
import com.erp.model.procurement.PurchaseOrder;
import com.erp.model.procurement.PurchaseOrderItem;
import com.erp.model.procurement.Vendor;
import com.erp.repository.*;
import com.erp.repository.*;
import com.erp.repository.*;
import com.erp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProcurementService {

    private final VendorRepository vendorRepo;
    private final PurchaseOrderRepository poRepo;
    private final InventoryService inventoryService;
    private final FinanceService financeService;
    private final InvoiceRepository invoiceRepo;
    private final AuditLogRepository auditRepo;

    public ProcurementService(VendorRepository vendorRepo,
                              PurchaseOrderRepository poRepo,
                              InventoryService inventoryService,
                              FinanceService financeService,
                              InvoiceRepository invoiceRepo,
                              AuditLogRepository auditRepo) {
        this.vendorRepo = vendorRepo;
        this.poRepo = poRepo;
        this.inventoryService = inventoryService;
        this.financeService = financeService;
        this.invoiceRepo = invoiceRepo;
        this.auditRepo = auditRepo;
    }

    public List<Vendor> getAllVendors() {
        return vendorRepo.findAll();
    }

    public Vendor saveVendor(Vendor vendor) {
        return vendorRepo.save(vendor);
    }

    public List<PurchaseOrder> getAllPurchaseOrders() {
        return poRepo.findAll();
    }

    public PurchaseOrder getPurchaseOrderById(Long id) {
        return poRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase Order not found: " + id));
    }

    public PurchaseOrder savePurchaseOrder(PurchaseOrder po) {
        if (po.getItems() != null) {
            po.getItems().forEach(item -> item.setPurchaseOrder(po));
            po.recalculateTotal();
        }
        PurchaseOrder saved = poRepo.save(po);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.PROCUREMENT, "CREATE_PO",
                saved.getPoNumber(), "Procurement Officer",
                "Created PO for vendor " + (saved.getVendor() != null ? saved.getVendor().getCompanyName() : "N/A")));
        return saved;
    }

    public PurchaseOrder approvePurchaseOrder(Long poId) {
        PurchaseOrder po = getPurchaseOrderById(poId);
        po.setStatus(PurchaseOrder.PoStatus.APPROVED);
        PurchaseOrder saved = poRepo.save(po);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.PROCUREMENT, "APPROVE_PO",
                saved.getPoNumber(), "Procurement Manager", "Approved PO for $" + saved.getTotalAmount()));
        return saved;
    }

    @Transactional
    public PurchaseOrder receivePurchaseOrder(Long poId) {
        PurchaseOrder po = getPurchaseOrderById(poId);
        if (po.getStatus() == PurchaseOrder.PoStatus.RECEIVED) {
            throw new IllegalStateException("Purchase Order " + po.getPoNumber() + " is already received.");
        }

        // 1. Replenish inventory for all items in PO
        for (PurchaseOrderItem item : po.getItems()) {
            inventoryService.recordMovement(
                    item.getProduct().getId(),
                    StockMovement.MovementType.INWARD_PURCHASE,
                    item.getQuantity(),
                    po.getPoNumber(),
                    "Goods received from vendor: " + po.getVendor().getCompanyName()
            );
        }

        // 2. Generate AP Invoice
        Invoice invoice = new Invoice(
                "BILL-" + po.getPoNumber(),
                Invoice.InvoiceType.AP_PURCHASE,
                po.getVendor().getCompanyName(),
                po.getPoNumber(),
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                po.getTotalAmount()
        );
        invoiceRepo.save(invoice);

        // 3. Post Finance Journal Entry: Debit Inventory Asset ("1030"), Credit Accounts Payable ("2010")
        Account invAcc = financeService.getAccountByCode("1030");
        Account apAcc = financeService.getAccountByCode("2010");

        JournalEntry je = new JournalEntry(
                "JE-PO-" + po.getPoNumber(),
                LocalDate.now(),
                "Procurement Goods Receipt for " + po.getPoNumber(),
                "PROCUREMENT",
                po.getPoNumber()
        );
        je.addLine(new JournalLine(invAcc, po.getTotalAmount(), BigDecimal.ZERO, "Inventory asset increase"));
        je.addLine(new JournalLine(apAcc, BigDecimal.ZERO, po.getTotalAmount(), "Vendor payable"));
        financeService.createJournalEntry(je);

        // 4. Update Vendor balance due
        Vendor vendor = po.getVendor();
        vendor.setBalanceDue(vendor.getBalanceDue().add(po.getTotalAmount()));
        vendorRepo.save(vendor);

        // 5. Update PO status
        po.setStatus(PurchaseOrder.PoStatus.RECEIVED);
        PurchaseOrder updatedPo = poRepo.save(po);

        auditRepo.save(new AuditLog(AuditLog.ModuleName.PROCUREMENT, "RECEIVE_PO_GOODS",
                po.getPoNumber(), "Warehouse / Procurement",
                "Automated goods receipt, inventory stocked, AP bill generated, and GL posted."));

        return updatedPo;
    }
}
