package com.erp.service;

import com.erp.model.audit.AuditLog;
import com.erp.model.hr.Employee;
import com.erp.model.manufacturing.WorkOrder;
import com.erp.model.procurement.PurchaseOrder;
import com.erp.model.sales.SalesOrder;
import com.erp.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final FinanceService financeService;
    private final InventoryService inventoryService;
    private final EmployeeRepository empRepo;
    private final SalesOrderRepository soRepo;
    private final PurchaseOrderRepository poRepo;
    private final WorkOrderRepository woRepo;
    private final AuditLogRepository auditRepo;
    private final DepartmentRepository deptRepo;

    @Value("${erp.deployment.mode:HYBRID}")
    private String deploymentMode;

    @Value("${erp.deployment.cloud-provider:AWS / Azure / GCP Compatible}")
    private String cloudProvider;

    public DashboardService(FinanceService financeService,
                            InventoryService inventoryService,
                            EmployeeRepository empRepo,
                            SalesOrderRepository soRepo,
                            PurchaseOrderRepository poRepo,
                            WorkOrderRepository woRepo,
                            AuditLogRepository auditRepo,
                            DepartmentRepository deptRepo) {
        this.financeService = financeService;
        this.inventoryService = inventoryService;
        this.empRepo = empRepo;
        this.soRepo = soRepo;
        this.poRepo = poRepo;
        this.woRepo = woRepo;
        this.auditRepo = auditRepo;
        this.deptRepo = deptRepo;
    }

    public Map<String, Object> getDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Financial KPIs
        Map<String, Object> fin = financeService.getFinancialSummary();
        metrics.put("financials", fin);

        // Inventory KPIs
        metrics.put("inventoryValuation", inventoryService.getTotalInventoryValuation());
        metrics.put("totalProducts", inventoryService.getAllProducts().size());
        metrics.put("lowStockCount", inventoryService.getLowStockAlerts().size());

        // HR KPIs
        long activeHeadcount = empRepo.countByStatus(Employee.EmployeeStatus.ACTIVE);
        metrics.put("activeEmployees", activeHeadcount);
        metrics.put("totalDepartments", deptRepo.count());

        // Operations KPIs
        long pendingSalesOrders = soRepo.findAll().stream()
                .filter(so -> so.getStatus() == SalesOrder.SoStatus.PENDING || so.getStatus() == SalesOrder.SoStatus.CONFIRMED)
                .count();
        long openPurchaseOrders = poRepo.findAll().stream()
                .filter(po -> po.getStatus() == PurchaseOrder.PoStatus.DRAFT || po.getStatus() == PurchaseOrder.PoStatus.APPROVED)
                .count();
        long activeWorkOrders = woRepo.findAll().stream()
                .filter(wo -> wo.getStatus() == WorkOrder.WorkOrderStatus.PLANNED || wo.getStatus() == WorkOrder.WorkOrderStatus.IN_PROGRESS)
                .count();

        metrics.put("openSalesOrders", pendingSalesOrders);
        metrics.put("openPurchaseOrders", openPurchaseOrders);
        metrics.put("activeWorkOrders", activeWorkOrders);

        // Real-time Event Stream / Audit Logs
        List<AuditLog> recentAudits = auditRepo.findTop100ByOrderByTimestampDesc();
        metrics.put("recentAudits", recentAudits);

        // Architecture & Deployment Info
        Map<String, String> deployInfo = new HashMap<>();
        deployInfo.put("mode", deploymentMode);
        deployInfo.put("cloudProvider", cloudProvider);
        deployInfo.put("database", "Central Unified Relational Database (Single Source of Truth)");
        deployInfo.put("realTimeAutomation", "Event-Driven Cross-Module Sync Active");
        metrics.put("deployment", deployInfo);

        return metrics;
    }
}
