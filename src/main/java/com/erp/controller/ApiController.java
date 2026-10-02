package com.erp.controller;

import com.erp.model.audit.AuditLog;
import com.erp.model.finance.Account;
import com.erp.model.finance.Invoice;
import com.erp.model.finance.JournalEntry;
import com.erp.model.hr.Department;
import com.erp.model.hr.Employee;
import com.erp.model.hr.JobRequisition;
import com.erp.model.hr.LeaveRequest;
import com.erp.model.hr.Payroll;
import com.erp.model.hr.RecruitmentChannel;
import com.erp.model.hr.RecruitmentPost;
import com.erp.model.inventory.Product;
import com.erp.model.inventory.StockMovement;
import com.erp.model.manufacturing.BillOfMaterial;
import com.erp.model.manufacturing.WorkOrder;
import com.erp.model.procurement.PurchaseOrder;
import com.erp.model.procurement.Vendor;
import com.erp.model.sales.Customer;
import com.erp.model.sales.SalesOrder;
import com.erp.repository.*;
import com.erp.service.*;
import com.erp.model.auth.Role;
import com.erp.model.auth.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    private final DashboardService dashboardService;
    private final FinanceService financeService;
    private final HrService hrService;
    private final RecruitmentService recruitmentService;
    private final InventoryService inventoryService;
    private final ProcurementService procurementService;
    private final SalesService salesService;
    private final ManufacturingService manufacturingService;
    private final AuthAdminService authAdminService;
    private final AuditLogRepository auditRepo;
    private final ObjectMapper objectMapper;

    public ApiController(DashboardService dashboardService,
                         FinanceService financeService,
                         HrService hrService,
                         RecruitmentService recruitmentService,
                         InventoryService inventoryService,
                         ProcurementService procurementService,
                         SalesService salesService,
                         ManufacturingService manufacturingService,
                         AuthAdminService authAdminService,
                         AuditLogRepository auditRepo,
                         ObjectMapper objectMapper) {
        this.dashboardService = dashboardService;
        this.financeService = financeService;
        this.hrService = hrService;
        this.recruitmentService = recruitmentService;
        this.inventoryService = inventoryService;
        this.procurementService = procurementService;
        this.salesService = salesService;
        this.manufacturingService = manufacturingService;
        this.authAdminService = authAdminService;
        this.auditRepo = auditRepo;
        this.objectMapper = objectMapper;
    }

    // ==========================================
    // 1. DASHBOARD & AUDIT
    // ==========================================
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboardMetrics());
    }

    @GetMapping("/audit")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditRepo.findTop100ByOrderByTimestampDesc());
    }

    // ==========================================
    // 2. FINANCE & ACCOUNTING
    // ==========================================
    @GetMapping("/finance/accounts")
    public ResponseEntity<List<Account>> getAccounts() {
        return ResponseEntity.ok(financeService.getAllAccounts());
    }

    @PostMapping("/finance/accounts")
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        return ResponseEntity.ok(financeService.createAccount(account));
    }

    @GetMapping("/finance/journal-entries")
    public ResponseEntity<List<JournalEntry>> getJournalEntries() {
        return ResponseEntity.ok(financeService.getAllJournalEntries());
    }

    @PostMapping("/finance/journal-entries")
    public ResponseEntity<JournalEntry> createJournalEntry(@RequestBody JournalEntry entry) {
        return ResponseEntity.ok(financeService.createJournalEntry(entry));
    }

    @GetMapping("/finance/invoices")
    public ResponseEntity<List<Invoice>> getInvoices() {
        return ResponseEntity.ok(financeService.getAllInvoices());
    }

    @PostMapping("/finance/invoices/{id}/pay")
    public ResponseEntity<Invoice> payInvoice(@PathVariable Long id, @RequestBody Map<String, BigDecimal> body) {
        BigDecimal amount = body.getOrDefault("amount", BigDecimal.ZERO);
        return ResponseEntity.ok(financeService.recordPayment(id, amount));
    }

    // ==========================================
    // 3. HUMAN RESOURCES (HR)
    // ==========================================
    @GetMapping("/hr/departments")
    public ResponseEntity<List<Department>> getDepartments() {
        return ResponseEntity.ok(hrService.getAllDepartments());
    }

    @PostMapping("/hr/departments")
    public ResponseEntity<Department> createDepartment(@RequestBody Department dept) {
        return ResponseEntity.ok(hrService.saveDepartment(dept));
    }

    @GetMapping("/hr/employees")
    public ResponseEntity<List<Employee>> getEmployees() {
        return ResponseEntity.ok(hrService.getAllEmployees());
    }

    @PostMapping("/hr/employees")
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        return ResponseEntity.ok(hrService.saveEmployee(employee));
    }

    @GetMapping("/hr/payrolls")
    public ResponseEntity<List<Payroll>> getPayrolls() {
        return ResponseEntity.ok(hrService.getAllPayrolls());
    }

    @PostMapping("/hr/payrolls/run")
    public ResponseEntity<List<Payroll>> runPayroll(@RequestBody Map<String, String> body) {
        String period = body.getOrDefault("period", "2026-10");
        return ResponseEntity.ok(hrService.runMonthlyPayroll(period));
    }

    @GetMapping("/hr/leaves")
    public ResponseEntity<List<LeaveRequest>> getLeaveRequests() {
        return ResponseEntity.ok(hrService.getAllLeaveRequests());
    }

    @PostMapping("/hr/leaves")
    public ResponseEntity<LeaveRequest> createLeaveRequest(@RequestBody LeaveRequest request) {
        return ResponseEntity.ok(hrService.saveLeaveRequest(request));
    }

    @PostMapping("/hr/leaves/{id}/status")
    public ResponseEntity<LeaveRequest> updateLeaveStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.getOrDefault("status", "APPROVED");
        return ResponseEntity.ok(hrService.updateLeaveStatus(id, LeaveRequest.LeaveStatus.valueOf(status)));
    }

    @GetMapping("/hr/requisitions")
    public ResponseEntity<List<JobRequisition>> getJobRequisitions() {
        return ResponseEntity.ok(hrService.getAllJobRequisitions());
    }

    @PostMapping("/hr/requisitions")
    public ResponseEntity<JobRequisition> createJobRequisition(@RequestBody JobRequisition requisition) {
        return ResponseEntity.ok(hrService.saveJobRequisition(requisition));
    }

    @PostMapping("/hr/requisitions/{id}/status")
    public ResponseEntity<JobRequisition> updateJobRequisitionStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String statusStr = body.getOrDefault("status", "APPROVED");
        return ResponseEntity.ok(hrService.updateRequisitionStatus(id, JobRequisition.RequisitionStatus.valueOf(statusStr)));
    }

    // ==========================================
    // 3B. RECRUITMENT & MULTI-SECTOR BROADCAST
    // ==========================================
    @GetMapping("/recruitment/channels")
    public ResponseEntity<List<RecruitmentChannel>> getRecruitmentChannels() {
        return ResponseEntity.ok(recruitmentService.getAllChannels());
    }

    @PostMapping("/recruitment/channels/{id}/toggle")
    public ResponseEntity<RecruitmentChannel> toggleRecruitmentChannel(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Boolean connected = body.get("connected");
        Boolean autoPost = body.get("autoPost");
        return ResponseEntity.ok(recruitmentService.toggleChannel(id, connected, autoPost));
    }

    @GetMapping("/recruitment/posts")
    public ResponseEntity<List<RecruitmentPost>> getRecruitmentPosts() {
        return ResponseEntity.ok(recruitmentService.getAllPosts());
    }

    @PostMapping("/recruitment/posts")
    public ResponseEntity<RecruitmentPost> createRecruitmentPost(@RequestBody Map<String, Object> payload) {
        Object postObj = payload.containsKey("post") ? payload.get("post") : payload;
        RecruitmentPost post = objectMapper.convertValue(postObj, RecruitmentPost.class);
        if (post.getDepartment() == null) {
            Long deptId = null;
            if (payload.get("departmentId") != null) {
                deptId = ((Number) payload.get("departmentId")).longValue();
            } else if (postObj instanceof Map<?, ?> map && map.get("departmentId") != null) {
                deptId = ((Number) map.get("departmentId")).longValue();
            }
            if (deptId != null) {
                Department dept = new Department();
                dept.setId(deptId);
                post.setDepartment(dept);
            }
        }
        List<String> channels = null;
        if (payload.containsKey("channels") && payload.get("channels") instanceof List<?>) {
            channels = ((List<?>) payload.get("channels")).stream().map(Object::toString).toList();
        }
        return ResponseEntity.ok(recruitmentService.createAndBroadcastPost(post, channels));
    }

    @PostMapping("/recruitment/posts/{id}/close")
    public ResponseEntity<RecruitmentPost> closeRecruitmentPost(@PathVariable Long id) {
        return ResponseEntity.ok(recruitmentService.closePost(id));
    }

    @PostMapping("/recruitment/posts/{id}/simulate-applicant")
    public ResponseEntity<RecruitmentPost> simulateApplicant(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String channel = (String) body.getOrDefault("channelCode", "LINKEDIN");
        int count = body.containsKey("count") ? ((Number) body.get("count")).intValue() : 1;
        return ResponseEntity.ok(recruitmentService.simulateEngagement(id, channel, count));
    }

    // ==========================================
    // 4. INVENTORY & WAREHOUSE
    // ==========================================
    @GetMapping("/inventory/products")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(inventoryService.getAllProducts());
    }

    @PostMapping("/inventory/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(inventoryService.saveProduct(product));
    }

    @GetMapping("/inventory/low-stock")
    public ResponseEntity<List<Product>> getLowStock() {
        return ResponseEntity.ok(inventoryService.getLowStockAlerts());
    }

    @GetMapping("/inventory/movements")
    public ResponseEntity<List<StockMovement>> getStockMovements() {
        return ResponseEntity.ok(inventoryService.getRecentMovements());
    }

    // ==========================================
    // 5. PROCUREMENT & SUPPLY CHAIN
    // ==========================================
    @GetMapping("/procurement/vendors")
    public ResponseEntity<List<Vendor>> getVendors() {
        return ResponseEntity.ok(procurementService.getAllVendors());
    }

    @PostMapping("/procurement/vendors")
    public ResponseEntity<Vendor> createVendor(@RequestBody Vendor vendor) {
        return ResponseEntity.ok(procurementService.saveVendor(vendor));
    }

    @GetMapping("/procurement/orders")
    public ResponseEntity<List<PurchaseOrder>> getPurchaseOrders() {
        return ResponseEntity.ok(procurementService.getAllPurchaseOrders());
    }

    @PostMapping("/procurement/orders")
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@RequestBody PurchaseOrder po) {
        return ResponseEntity.ok(procurementService.savePurchaseOrder(po));
    }

    @PostMapping("/procurement/orders/{id}/approve")
    public ResponseEntity<PurchaseOrder> approvePurchaseOrder(@PathVariable Long id) {
        return ResponseEntity.ok(procurementService.approvePurchaseOrder(id));
    }

    @PostMapping("/procurement/orders/{id}/receive")
    public ResponseEntity<PurchaseOrder> receivePurchaseOrder(@PathVariable Long id) {
        return ResponseEntity.ok(procurementService.receivePurchaseOrder(id));
    }

    // ==========================================
    // 6. SALES & CRM
    // ==========================================
    @GetMapping("/sales/customers")
    public ResponseEntity<List<Customer>> getCustomers() {
        return ResponseEntity.ok(salesService.getAllCustomers());
    }

    @PostMapping("/sales/customers")
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        return ResponseEntity.ok(salesService.saveCustomer(customer));
    }

    @GetMapping("/sales/orders")
    public ResponseEntity<List<SalesOrder>> getSalesOrders() {
        return ResponseEntity.ok(salesService.getAllSalesOrders());
    }

    @PostMapping("/sales/orders")
    public ResponseEntity<SalesOrder> createSalesOrder(@RequestBody SalesOrder so) {
        return ResponseEntity.ok(salesService.saveSalesOrder(so));
    }

    @PostMapping("/sales/orders/{id}/confirm")
    public ResponseEntity<SalesOrder> confirmSalesOrder(@PathVariable Long id) {
        return ResponseEntity.ok(salesService.confirmSalesOrder(id));
    }

    @PostMapping("/sales/orders/{id}/fulfill")
    public ResponseEntity<SalesOrder> fulfillSalesOrder(@PathVariable Long id) {
        return ResponseEntity.ok(salesService.fulfillSalesOrder(id));
    }

    // ==========================================
    // 7. MANUFACTURING & PRODUCTION
    // ==========================================
    @GetMapping("/manufacturing/boms")
    public ResponseEntity<List<BillOfMaterial>> getBoms() {
        return ResponseEntity.ok(manufacturingService.getAllBoms());
    }

    @PostMapping("/manufacturing/boms")
    public ResponseEntity<BillOfMaterial> createBom(@RequestBody BillOfMaterial bom) {
        return ResponseEntity.ok(manufacturingService.saveBom(bom));
    }

    @GetMapping("/manufacturing/work-orders")
    public ResponseEntity<List<WorkOrder>> getWorkOrders() {
        return ResponseEntity.ok(manufacturingService.getAllWorkOrders());
    }

    @PostMapping("/manufacturing/work-orders")
    public ResponseEntity<WorkOrder> createWorkOrder(@RequestBody WorkOrder wo) {
        return ResponseEntity.ok(manufacturingService.saveWorkOrder(wo));
    }

    @PostMapping("/manufacturing/work-orders/{id}/start")
    public ResponseEntity<WorkOrder> startWorkOrder(@PathVariable Long id) {
        return ResponseEntity.ok(manufacturingService.startWorkOrder(id));
    }

    @PostMapping("/manufacturing/work-orders/{id}/complete")
    public ResponseEntity<WorkOrder> completeWorkOrder(@PathVariable Long id, @RequestBody(required = false) Map<String, Integer> body) {
        Integer qty = body != null ? body.get("quantity") : null;
        return ResponseEntity.ok(manufacturingService.completeWorkOrder(id, qty));
    }

    // ==========================================
    // 7. AUTHENTICATION & SESSION MANAGEMENT
    // ==========================================
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        try {
            String username = creds.get("username");
            String password = creds.get("password");
            return ResponseEntity.ok(authAdminService.login(username, password));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                   @RequestBody(required = false) Map<String, String> body) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (body != null && body.containsKey("token")) {
            token = body.get("token");
        }
        if (token != null) {
            authAdminService.logout(token);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/auth/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                           @RequestParam(value = "token", required = false) String tokenParam) {
        String token = tokenParam;
        if (token == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        Map<String, Object> user = authAdminService.getCurrentUser(token);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized or invalid session"));
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/auth/demo-users")
    public ResponseEntity<List<Map<String, Object>>> getDemoUsers() {
        return ResponseEntity.ok(authAdminService.getDemoAccounts());
    }

    // ==========================================
    // 8. ROLES & ADMINISTRATION
    // ==========================================
    @GetMapping("/admin/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(authAdminService.getAllUsers());
    }

    @PostMapping("/admin/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> userData) {
        try {
            return ResponseEntity.ok(authAdminService.createUser(userData));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/admin/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userData) {
        try {
            return ResponseEntity.ok(authAdminService.updateUser(id, userData));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/admin/users/{id}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(authAdminService.toggleUserStatus(id));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            authAdminService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(authAdminService.getAllRoles());
    }

    @PostMapping("/admin/roles")
    public ResponseEntity<?> createRole(@RequestBody Role role) {
        try {
            return ResponseEntity.ok(authAdminService.createRole(role));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/admin/roles/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Role role) {
        return ResponseEntity.ok(authAdminService.updateRole(id, role));
    }

    @GetMapping("/admin/security-overview")
    public ResponseEntity<Map<String, Object>> getSecurityOverview() {
        return ResponseEntity.ok(authAdminService.getSecurityOverview());
    }

    @GetMapping("/admin/audit-logs")
    public ResponseEntity<List<AuditLog>> getAdminAuditLogs() {
        return ResponseEntity.ok(authAdminService.getAdminAuditLogs());
    }
}
