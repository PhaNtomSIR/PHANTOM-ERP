package com.erp.service;

import com.erp.model.audit.AuditLog;
import com.erp.model.finance.Account;
import com.erp.model.finance.Invoice;
import com.erp.model.finance.JournalEntry;
import com.erp.model.finance.JournalLine;
import com.erp.model.inventory.StockMovement;
import com.erp.model.sales.Customer;
import com.erp.model.sales.SalesOrder;
import com.erp.model.sales.SalesOrderItem;
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
public class SalesService {

    private final CustomerRepository customerRepo;
    private final SalesOrderRepository soRepo;
    private final InventoryService inventoryService;
    private final FinanceService financeService;
    private final InvoiceRepository invoiceRepo;
    private final AuditLogRepository auditRepo;

    public SalesService(CustomerRepository customerRepo,
                        SalesOrderRepository soRepo,
                        InventoryService inventoryService,
                        FinanceService financeService,
                        InvoiceRepository invoiceRepo,
                        AuditLogRepository auditRepo) {
        this.customerRepo = customerRepo;
        this.soRepo = soRepo;
        this.inventoryService = inventoryService;
        this.financeService = financeService;
        this.invoiceRepo = invoiceRepo;
        this.auditRepo = auditRepo;
    }

    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepo.save(customer);
    }

    public List<SalesOrder> getAllSalesOrders() {
        return soRepo.findAll();
    }

    public SalesOrder getSalesOrderById(Long id) {
        return soRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sales Order not found: " + id));
    }

    public SalesOrder saveSalesOrder(SalesOrder so) {
        if (so.getItems() != null) {
            so.getItems().forEach(item -> item.setSalesOrder(so));
            so.recalculateTotal();
        }
        SalesOrder saved = soRepo.save(so);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.SALES, "CREATE_SALES_ORDER",
                saved.getSoNumber(), "Sales Rep",
                "Created order for customer " + (saved.getCustomer() != null ? saved.getCustomer().getName() : "N/A")));
        return saved;
    }

    public SalesOrder confirmSalesOrder(Long soId) {
        SalesOrder so = getSalesOrderById(soId);
        so.setStatus(SalesOrder.SoStatus.CONFIRMED);
        SalesOrder saved = soRepo.save(so);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.SALES, "CONFIRM_SO",
                saved.getSoNumber(), "Sales Manager", "Order confirmed, pending fulfillment."));
        return saved;
    }

    @Transactional
    public SalesOrder fulfillSalesOrder(Long soId) {
        SalesOrder so = getSalesOrderById(soId);
        if (so.getStatus() == SalesOrder.SoStatus.SHIPPED) {
            throw new IllegalStateException("Sales Order " + so.getSoNumber() + " has already been shipped.");
        }

        BigDecimal totalCogs = BigDecimal.ZERO;

        // 1. Deduct Inventory for items and track COGS
        for (SalesOrderItem item : so.getItems()) {
            inventoryService.recordMovement(
                    item.getProduct().getId(),
                    StockMovement.MovementType.OUTWARD_SALES,
                    -item.getQuantity(),
                    so.getSoNumber(),
                    "Fulfillment for customer " + so.getCustomer().getName()
            );

            BigDecimal itemCost = item.getProduct().getUnitCost() != null ?
                    item.getProduct().getUnitCost().multiply(BigDecimal.valueOf(item.getQuantity())) : BigDecimal.ZERO;
            totalCogs = totalCogs.add(itemCost);
        }

        // 2. Generate AR Invoice
        Invoice invoice = new Invoice(
                "INV-" + so.getSoNumber(),
                Invoice.InvoiceType.AR_SALES,
                so.getCustomer().getName(),
                so.getSoNumber(),
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                so.getTotalAmount()
        );
        invoiceRepo.save(invoice);

        // 3. Post Finance Journal Entries:
        // Entry 1: AR & Revenue (Debit AR 1020, Credit Revenue 4010)
        Account arAcc = financeService.getAccountByCode("1020");
        Account revAcc = financeService.getAccountByCode("4010");
        Account cogsAcc = financeService.getAccountByCode("5010");
        Account invAcc = financeService.getAccountByCode("1030");

        JournalEntry je = new JournalEntry(
                "JE-SO-" + so.getSoNumber(),
                LocalDate.now(),
                "Sales Order Fulfillment for " + so.getSoNumber(),
                "SALES",
                so.getSoNumber()
        );
        je.addLine(new JournalLine(arAcc, so.getTotalAmount(), BigDecimal.ZERO, "Receivable from " + so.getCustomer().getName()));
        je.addLine(new JournalLine(revAcc, BigDecimal.ZERO, so.getTotalAmount(), "Sales Revenue"));

        if (totalCogs.compareTo(BigDecimal.ZERO) > 0) {
            je.addLine(new JournalLine(cogsAcc, totalCogs, BigDecimal.ZERO, "Cost of Goods Sold"));
            je.addLine(new JournalLine(invAcc, BigDecimal.ZERO, totalCogs, "Inventory Reduction at Cost"));
        }
        financeService.createJournalEntry(je);

        // 4. Update Customer Outstanding Balance
        Customer cust = so.getCustomer();
        cust.setOutstandingBalance(cust.getOutstandingBalance().add(so.getTotalAmount()));
        customerRepo.save(cust);

        // 5. Update Status
        so.setStatus(SalesOrder.SoStatus.SHIPPED);
        SalesOrder updatedSo = soRepo.save(so);

        auditRepo.save(new AuditLog(AuditLog.ModuleName.SALES, "SHIP_SALES_ORDER",
                so.getSoNumber(), "Logistics / Sales",
                "Automated order fulfillment: inventory dispatched, AR invoice generated, revenue & COGS recognized."));

        return updatedSo;
    }
}
