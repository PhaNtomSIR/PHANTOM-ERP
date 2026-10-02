package com.erp.config;

import com.erp.model.audit.AuditLog;
import com.erp.model.finance.Account;
import com.erp.model.hr.Department;
import com.erp.model.hr.Employee;
import com.erp.model.hr.JobRequisition;
import com.erp.model.hr.LeaveRequest;
import com.erp.model.inventory.Product;
import com.erp.model.manufacturing.BillOfMaterial;
import com.erp.model.manufacturing.BomComponent;
import com.erp.model.manufacturing.WorkOrder;
import com.erp.model.procurement.PurchaseOrder;
import com.erp.model.procurement.PurchaseOrderItem;
import com.erp.model.procurement.Vendor;
import com.erp.model.sales.Customer;
import com.erp.model.sales.SalesOrder;
import com.erp.model.sales.SalesOrderItem;
import com.erp.repository.*;
import com.erp.service.*;
import com.erp.model.hr.RecruitmentChannel;
import com.erp.model.hr.RecruitmentPost;
import com.erp.model.auth.Role;
import com.erp.model.auth.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepo;
    private final DepartmentRepository deptRepo;
    private final EmployeeRepository empRepo;
    private final LeaveRequestRepository leaveRepo;
    private final ProductRepository productRepo;
    private final VendorRepository vendorRepo;
    private final CustomerRepository customerRepo;
    private final BillOfMaterialRepository bomRepo;
    private final WorkOrderRepository woRepo;
    private final AuditLogRepository auditRepo;

    private final ProcurementService procurementService;
    private final SalesService salesService;
    private final ManufacturingService manufacturingService;
    private final HrService hrService;
    private final RecruitmentService recruitmentService;
    private final RoleRepository roleRepo;
    private final UserRepository userRepo;

    public DataInitializer(AccountRepository accountRepo,
                           DepartmentRepository deptRepo,
                           EmployeeRepository empRepo,
                           LeaveRequestRepository leaveRepo,
                           ProductRepository productRepo,
                           VendorRepository vendorRepo,
                           CustomerRepository customerRepo,
                           BillOfMaterialRepository bomRepo,
                           WorkOrderRepository woRepo,
                           AuditLogRepository auditRepo,
                           ProcurementService procurementService,
                           SalesService salesService,
                           ManufacturingService manufacturingService,
                           HrService hrService,
                           RecruitmentService recruitmentService,
                           RoleRepository roleRepo,
                           UserRepository userRepo) {
        this.accountRepo = accountRepo;
        this.deptRepo = deptRepo;
        this.empRepo = empRepo;
        this.leaveRepo = leaveRepo;
        this.productRepo = productRepo;
        this.vendorRepo = vendorRepo;
        this.customerRepo = customerRepo;
        this.bomRepo = bomRepo;
        this.woRepo = woRepo;
        this.auditRepo = auditRepo;
        this.procurementService = procurementService;
        this.salesService = salesService;
        this.manufacturingService = manufacturingService;
        this.hrService = hrService;
        this.recruitmentService = recruitmentService;
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
    }

    @Override
    public void run(String... args) {
        if (accountRepo.count() > 0) return;

        System.out.println(">>> Initializing Central Database with Enterprise ERP Seed Data...");

        // 1. Chart of Accounts
        Account accCash = accountRepo.save(new Account("1010", "Cash and Operating Bank Account", Account.AccountType.ASSET, new BigDecimal("250000.00"), "Primary checking account"));
        Account accAr = accountRepo.save(new Account("1020", "Accounts Receivable (Trade)", Account.AccountType.ASSET, BigDecimal.ZERO, "Receivables from customers"));
        Account accInv = accountRepo.save(new Account("1030", "Inventory Asset (Raw Materials & FG)", Account.AccountType.ASSET, new BigDecimal("85000.00"), "Stock value on hand"));
        Account accAp = accountRepo.save(new Account("2010", "Accounts Payable (Vendors)", Account.AccountType.LIABILITY, BigDecimal.ZERO, "Owed to suppliers"));
        Account accTax = accountRepo.save(new Account("2020", "Payroll Tax & Withholding Liabilities", Account.AccountType.LIABILITY, BigDecimal.ZERO, "State & federal tax payable"));
        Account accEq = accountRepo.save(new Account("3010", "Common Stock & Retained Earnings", Account.AccountType.EQUITY, new BigDecimal("335000.00"), "Owner equity"));
        Account accRev = accountRepo.save(new Account("4010", "Sales Revenue - Finished Goods", Account.AccountType.REVENUE, BigDecimal.ZERO, "Revenue from commercial sales"));
        Account accCogs = accountRepo.save(new Account("5010", "Cost of Goods Sold (COGS)", Account.AccountType.EXPENSE, BigDecimal.ZERO, "Direct costs of goods sold"));
        Account accSal = accountRepo.save(new Account("5020", "Salaries & Employee Compensation", Account.AccountType.EXPENSE, BigDecimal.ZERO, "Wages and payroll"));
        Account accLabor = accountRepo.save(new Account("5030", "Direct Manufacturing Labor", Account.AccountType.EXPENSE, BigDecimal.ZERO, "Factory shop floor labor"));

        // 2. Departments
        Department deptExec = deptRepo.save(new Department("DEP-EXEC", "Executive Management", "Alexander Wright", new BigDecimal("1200000"), "HQ Tower - Fl 14"));
        Department deptMfg = deptRepo.save(new Department("DEP-MFG", "Advanced Manufacturing & Plant", "Marcus Vance", new BigDecimal("3500000"), "Plant 1 - Silicon Valley"));
        Department deptSales = deptRepo.save(new Department("DEP-SALES", "Commercial & Enterprise Sales", "Elena Rostova", new BigDecimal("1800000"), "HQ Tower - Fl 8"));
        Department deptProc = deptRepo.save(new Department("DEP-PROC", "Procurement & Global Supply Chain", "Devon Cole", new BigDecimal("950000"), "Logistics Hub B"));
        Department deptHr = deptRepo.save(new Department("DEP-HR", "People Operations & HR", "Sarah Jenkins", new BigDecimal("600000"), "HQ Tower - Fl 5"));
        Department deptFin = deptRepo.save(new Department("DEP-FIN", "Corporate Finance & Treasury", "David Chen", new BigDecimal("750000"), "HQ Tower - Fl 12"));

        // 3. Employees
        Employee emp1 = empRepo.save(new Employee("EMP-1001", "Alexander", "Wright", "a.wright@enterprise.com", "+1-555-0101", "Chief Executive Officer", "Executive Leadership", deptExec, new BigDecimal("220000"), LocalDate.of(2021, 1, 15)));
        Employee emp2 = empRepo.save(new Employee("EMP-1002", "Elena", "Rostova", "e.rostova@enterprise.com", "+1-555-0102", "VP of Commercial Sales", "Enterprise Sales", deptSales, new BigDecimal("145000"), LocalDate.of(2021, 3, 1)));
        Employee emp3 = empRepo.save(new Employee("EMP-1003", "Marcus", "Vance", "m.vance@enterprise.com", "+1-555-0103", "Director of Plant Operations", "Plant Operations", deptMfg, new BigDecimal("135000"), LocalDate.of(2021, 6, 20)));
        Employee emp4 = empRepo.save(new Employee("EMP-1004", "Sarah", "Jenkins", "s.jenkins@enterprise.com", "+1-555-0104", "HR Director", "People & Talent", deptHr, new BigDecimal("115000"), LocalDate.of(2022, 2, 10)));
        Employee emp5 = empRepo.save(new Employee("EMP-1005", "David", "Chen", "d.chen@enterprise.com", "+1-555-0105", "Chief Financial Controller", "Corporate Treasury", deptFin, new BigDecimal("140000"), LocalDate.of(2022, 5, 4)));
        Employee emp6 = empRepo.save(new Employee("EMP-1006", "Rajesh", "Patel", "r.patel@enterprise.com", "+1-555-0106", "Senior Manufacturing Systems Engineer", "Robotics & Automation", deptMfg, new BigDecimal("98000"), LocalDate.of(2023, 1, 12)));
        Employee emp7 = empRepo.save(new Employee("EMP-1007", "Clara", "Oswald", "c.oswald@enterprise.com", "+1-555-0107", "Senior Enterprise Account Executive", "Commercial Accounts", deptSales, new BigDecimal("92000"), LocalDate.of(2023, 4, 18)));

        // Leave request
        leaveRepo.save(new LeaveRequest(emp6, "Annual", LocalDate.now().plusDays(10), LocalDate.now().plusDays(15), 5, "Family vacation"));

        // Job Requisitions (Formal internal hiring authorization requests)
        JobRequisition req1 = new JobRequisition("REQ-2026-001", "Autonomous Robotics Control Engineer", deptMfg, "Robotics & Automation",
                "Marcus Vance", 2, "Full-time", "Senior",
                new BigDecimal("110000"), new BigDecimal("135000"),
                "Expansion of AGV robotics manufacturing cell for automated assembly line",
                "Lead the implementation and validation of ROS2 control algorithms, motor drive interfaces, and safety telemetry.",
                LocalDate.now().plusMonths(1));
        req1.setStatus(JobRequisition.RequisitionStatus.APPROVED);
        hrService.saveJobRequisition(req1);

        JobRequisition req2 = new JobRequisition("REQ-2026-002", "Senior Enterprise Account Executive", deptSales, "Commercial Accounts",
                "Elena Rostova", 1, "Full-time", "Senior",
                new BigDecimal("95000"), new BigDecimal("120000"),
                "Territory expansion for industrial fleet sales in Western Region",
                "Drive revenue growth, handle high-value commercial accounts, and close enterprise fleet deals.",
                LocalDate.now().plusDays(45));
        req2.setStatus(JobRequisition.RequisitionStatus.OPEN);
        hrService.saveJobRequisition(req2);

        JobRequisition req3 = new JobRequisition("REQ-2026-003", "People Operations & Talent Specialist", deptHr, "People & Talent",
                "Sarah Jenkins", 1, "Full-time", "Mid-Senior",
                new BigDecimal("75000"), new BigDecimal("90000"),
                "Support rapid headcount scaling across engineering and manufacturing",
                "Coordinate full-cycle recruitment, manage job requisitions, candidate pipelines, and onboarding logistics.",
                LocalDate.now().plusDays(20));
        req3.setStatus(JobRequisition.RequisitionStatus.PENDING_APPROVAL);
        hrService.saveJobRequisition(req3);

        // Connected Recruitment Platforms / Channels (LinkedIn, BDJobs, Indeed, Careers Portal)
        recruitmentService.saveChannel(
                new RecruitmentChannel("LINKEDIN", "LinkedIn Talent Solutions", "PROFESSIONAL_NETWORK", "💼",
                        "PHANTOM-ERP Global Talent Solutions (Org ID: 772910)", true, true,
                        "https://api.linkedin.com/v2/jobs", "li_sec_live_9a8df72e"));

        recruitmentService.saveChannel(
                new RecruitmentChannel("BDJOBS", "BDJobs Corporate Employer Gateway", "REGIONAL_PORTAL", "🇧🇩",
                        "PHANTOM-ERP Technology Ltd (Employer ID: 94021)", true, true,
                        "https://api.bdjobs.com/corporate/v1/jobs", "bdj_live_token_77a90b"));

        recruitmentService.saveChannel(
                new RecruitmentChannel("INDEED", "Indeed Recruiter Network", "GLOBAL_BOARD", "🔍",
                        "PHANTOM-ERP Verified Corporate Sponsor Account", true, true,
                        "https://api.indeed.com/v2/sponsored-jobs", "ind_key_prod_31bb48"));

        recruitmentService.saveChannel(
                new RecruitmentChannel("CAREERS_PORTAL", "PHANTOM-ERP Corporate Careers Portal", "CORPORATE_WEBSITE", "🌐",
                        "careers.phantom-erp.cloud (Automated Central Sync)", true, true,
                        "https://careers.phantom-erp.cloud/api/v1/ingest", "internal_jwt_sys_9011"));

        // Seed Initial Multi-Platform Public Recruitment Postings
        RecruitmentPost post1 = new RecruitmentPost(
                "POST-2026-001", req1, "Autonomous Robotics Control Engineer",
                deptMfg, "Robotics & Automation", "Dhaka & Silicon Valley (Hybrid)",
                "Full-time", "Senior", "$110,000 - $135,000 / year",
                "Join PHANTOM-ERP's robotics division to build next-gen autonomous manufacturing AGVs.",
                "Design, integrate, and test autonomous navigation, ROS2 controllers, and safety LiDAR modules for smart factory robots.",
                "5+ years in Robotics / Embedded Systems, C++/Python, ROS/ROS2, real-time control loops.",
                "Comprehensive health & dental, equity grants, annual performance bonus, hybrid work flexibility.",
                LocalDate.now().plusDays(28));
        recruitmentService.createAndBroadcastPost(post1, List.of("LINKEDIN", "BDJOBS", "INDEED", "CAREERS_PORTAL"));
        recruitmentService.simulateEngagement(post1.getId(), "LINKEDIN", 4);
        recruitmentService.simulateEngagement(post1.getId(), "BDJOBS", 3);

        RecruitmentPost post2 = new RecruitmentPost(
                "POST-2026-002", req2, "Senior Enterprise Account Executive",
                deptSales, "Commercial Accounts", "Dhaka, Bangladesh / Regional Travel",
                "Full-time", "Senior", "$95,000 - $120,000 / year",
                "Lead enterprise software & robotics client acquisition across South Asia & APAC.",
                "Own end-to-end sales cycle, establish C-level relationships, and manage high-value ERP deployments.",
                "6+ years in B2B SaaS/Enterprise sales, track record of exceeding $1M+ quotas.",
                "Uncapped sales commission, global travel allowance, premium private health insurance.",
                LocalDate.now().plusDays(35));
        recruitmentService.createAndBroadcastPost(post2, List.of("LINKEDIN", "BDJOBS", "CAREERS_PORTAL"));
        recruitmentService.simulateEngagement(post2.getId(), "LINKEDIN", 2);
        recruitmentService.simulateEngagement(post2.getId(), "BDJOBS", 5);

        // 4. Products & Inventory
        Product pChassis = productRepo.save(new Product("RAW-CHASSIS", "Reinforced Titanium Chassis Unit", "High tensile alloy chassis for robotics and e-mobility", Product.ProductCategory.RAW_MATERIAL, "PCS", new BigDecimal("180.00"), new BigDecimal("0.00"), 45, 10, "Zone A - Bay 12"));
        Product pMotor = productRepo.save(new Product("RAW-MOTOR", "Brushless High-Torque 48V Motor", "Heavy duty industrial drive motor", Product.ProductCategory.RAW_MATERIAL, "PCS", new BigDecimal("95.00"), new BigDecimal("0.00"), 90, 20, "Zone B - Shelf 04"));
        Product pBattery = productRepo.save(new Product("RAW-BATTERY", "Lithium Ion Smart Battery Pack 20Ah", "Thermal guarded high-density energy pack", Product.ProductCategory.RAW_MATERIAL, "PCS", new BigDecimal("140.00"), new BigDecimal("0.00"), 60, 15, "Zone Hazardous 1"));
        Product pController = productRepo.save(new Product("COMP-PCB", "Autonomous Control Motherboard v4", "Embedded dual MCU control board with telemetry", Product.ProductCategory.COMPONENT, "PCS", new BigDecimal("75.00"), new BigDecimal("0.00"), 8, 15, "Clean Room Rack 3")); // Low stock!

        Product pScooter = productRepo.save(new Product("FG-SCOOTER-X", "Industrial Urban Scooter Model-X", "Fleet grade commercial electric scooter", Product.ProductCategory.FINISHED_GOOD, "PCS", new BigDecimal("490.00"), new BigDecimal("1150.00"), 24, 5, "Warehouse FG-01"));
        Product pRobot = productRepo.save(new Product("FG-AGV-ROBOT", "Smart Warehouse AGV Autonomous Robot", "LiDAR guided autonomous logistics transporter", Product.ProductCategory.FINISHED_GOOD, "PCS", new BigDecimal("820.00"), new BigDecimal("2400.00"), 12, 3, "Warehouse FG-02"));

        // 5. Vendors (Procurement)
        Vendor vApex = vendorRepo.save(new Vendor("VEN-001", "Apex Industrial Alloys Ltd", "James Sterling", "supplies@apexalloys.com", "+1-800-555-2739", "742 Foundry Blvd, Cleveland OH", "Net 30", 4.9));
        Vendor vVolt = vendorRepo.save(new Vendor("VEN-002", "VoltDynamics Energy Systems", "Karin Lindholm", "orders@voltdynamics.io", "+1-800-555-8658", "12 Nordic Way, Austin TX", "Net 15", 4.7));
        Vendor vMicro = vendorRepo.save(new Vendor("VEN-003", "Precision MicroElectronics Corp", "Hiroshi Tanaka", "sales@precisionmicro.jp", "+1-800-555-9922", "88 Tech Campus, San Jose CA", "Net 30", 4.8));

        // 6. Customers (Sales)
        Customer cMetro = customerRepo.save(new Customer("CUST-101", "Metro Mobility Fleet Systems", "Rachel Adams", "r.adams@metromobility.com", "+1-415-555-7001", "100 Embarcadero, San Francisco CA", new BigDecimal("100000")));
        Customer cSwift = customerRepo.save(new Customer("CUST-102", "Swift Express Freight Logistics", "Brian Miller", "b.miller@swiftfreight.com", "+1-312-555-9082", "500 Logistics Way, Chicago IL", new BigDecimal("150000")));
        Customer cAtlas = customerRepo.save(new Customer("CUST-103", "Atlas Automation Warehouses", "Hannah Davis", "hannah@atlasautomation.org", "+1-206-555-3419", "220 Industrial Ring, Seattle WA", new BigDecimal("200000")));

        // 7. Bills of Material (BOM)
        BillOfMaterial bomScooter = new BillOfMaterial("BOM-SCOOTER-X", pScooter, "Assembly recipe for Industrial Scooter Model-X", new BigDecimal("45.00"), 2.5);
        bomScooter.addComponent(new BomComponent(pChassis, 1));
        bomScooter.addComponent(new BomComponent(pMotor, 1));
        bomScooter.addComponent(new BomComponent(pBattery, 1));
        bomScooter.addComponent(new BomComponent(pController, 1));
        bomRepo.save(bomScooter);

        BillOfMaterial bomRobot = new BillOfMaterial("BOM-AGV-ROBOT", pRobot, "Assembly recipe for LiDAR Guided AGV Autonomous Robot", new BigDecimal("95.00"), 5.0);
        bomRobot.addComponent(new BomComponent(pChassis, 1));
        bomRobot.addComponent(new BomComponent(pMotor, 2));
        bomRobot.addComponent(new BomComponent(pBattery, 1));
        bomRobot.addComponent(new BomComponent(pController, 2));
        bomRepo.save(bomRobot);

        // 8. Work Order
        WorkOrder wo1 = new WorkOrder("WO-2026-001", bomScooter, 10, LocalDate.now().minusDays(3), LocalDate.now().plusDays(2), "Marcus Vance", "Priority production run for Q4 fleet delivery");
        wo1.setStatus(WorkOrder.WorkOrderStatus.IN_PROGRESS);
        woRepo.save(wo1);

        WorkOrder wo2 = new WorkOrder("WO-2026-002", bomRobot, 5, LocalDate.now().minusDays(1), LocalDate.now().plusDays(7), "Marcus Vance", "Smart factory order batch");
        woRepo.save(wo2);

        // 9. Purchase Order (Procurement)
        PurchaseOrder po1 = new PurchaseOrder("PO-2026-001", vMicro, LocalDate.now().minusDays(5), LocalDate.now().plusDays(1), "Emergency replenishment for low stock motherboard PCBs");
        po1.addItem(new PurchaseOrderItem(pController, 50, new BigDecimal("75.00")));
        po1.setStatus(PurchaseOrder.PoStatus.APPROVED);
        procurementService.savePurchaseOrder(po1);

        // 10. Sales Order (Sales)
        SalesOrder so1 = new SalesOrder("SO-2026-001", cMetro, LocalDate.now().minusDays(2), LocalDate.now().plusDays(4), "Commercial fleet expansion delivery");
        so1.addItem(new SalesOrderItem(pScooter, 5, new BigDecimal("1150.00")));
        so1.setStatus(SalesOrder.SoStatus.CONFIRMED);
        salesService.saveSalesOrder(so1);

        // Process initial HR Payroll for demonstration
        hrService.runMonthlyPayroll("2026-09");

        // 11. ROLES & ADMINISTRATION INITIALIZATION
        Role rSuperAdmin = new Role("ROLE_SUPER_ADMIN", "Super Administrator",
                "Full unrestricted access across all enterprise modules, system configuration, audit logs, and user governance.",
                true, Set.of("ALL_MODULES", "MODULE_DASHBOARD", "MODULE_HR", "MODULE_FINANCE", "MODULE_PROCUREMENT",
                "MODULE_SALES", "MODULE_MANUFACTURING", "MODULE_INVENTORY", "MODULE_ADMIN",
                "HR_REQUISITIONS", "HR_RECRUITMENT", "HR_PAYROLL", "FINANCE_POST", "ADMIN_USERS", "ADMIN_ROLES", "ADMIN_AUDIT"));

        Role rHrManager = new Role("ROLE_HR_MANAGER", "HR & Talent Director",
                "Workforce planning, employee directory, leave management, recruitment channel broadcasting, and job requisitions.",
                true, Set.of("MODULE_DASHBOARD", "MODULE_HR", "HR_REQUISITIONS", "HR_RECRUITMENT", "HR_PAYROLL"));

        Role rFinanceManager = new Role("ROLE_FINANCE_MANAGER", "Chief Financial Controller",
                "Chart of accounts, general ledger, accounts payable/receivable, invoicing, and financial statement consolidation.",
                true, Set.of("MODULE_DASHBOARD", "MODULE_FINANCE", "FINANCE_POST", "FINANCE_INVOICE"));

        Role rOpsManager = new Role("ROLE_OPERATIONS_MANAGER", "Supply Chain & Operations Lead",
                "Production work orders, bills of material, inventory stock management, purchase orders, and sales distribution.",
                true, Set.of("MODULE_DASHBOARD", "MODULE_PROCUREMENT", "MODULE_MANUFACTURING", "MODULE_INVENTORY", "MODULE_SALES"));

        Role rEmployee = new Role("ROLE_EMPLOYEE", "Enterprise Staff Member",
                "Employee self-service portal, internal requisition view, and leave request submission.",
                true, Set.of("MODULE_DASHBOARD", "EMPLOYEE_SELF_SERVICE"));

        roleRepo.saveAll(List.of(rSuperAdmin, rHrManager, rFinanceManager, rOpsManager, rEmployee));

        // Seed Enterprise Users
        User uAdmin = new User("admin", "admin123", "Dr. Alexander Wright", "alexander.wright@phantom-erp.cloud",
                "👨‍💼", "Chief Executive Officer & Founder", rSuperAdmin, deptExec, User.UserStatus.ACTIVE);

        User uHr = new User("sarah.hr", "hr123", "Sarah Jenkins", "sarah.jenkins@phantom-erp.cloud",
                "👩‍💼", "VP of People & Talent Acquisition", rHrManager, deptHr, User.UserStatus.ACTIVE);

        User uFin = new User("marcus.fin", "finance123", "Marcus Vance", "marcus.vance@phantom-erp.cloud",
                "👨‍💼", "Chief Financial Officer", rFinanceManager, deptFin, User.UserStatus.ACTIVE);

        User uOps = new User("david.ops", "ops123", "David Kim", "david.kim@phantom-erp.cloud",
                "👨‍💼", "Chief Operating Officer", rOpsManager, deptMfg, User.UserStatus.ACTIVE);

        User uSales = new User("elena.sales", "sales123", "Elena Rostova", "elena.rostova@phantom-erp.cloud",
                "👩‍💼", "Director of Enterprise Sales", rOpsManager, deptSales, User.UserStatus.ACTIVE);

        userRepo.saveAll(List.of(uAdmin, uHr, uFin, uOps, uSales));

        // Record Initial System Audit Log
        auditRepo.save(new AuditLog(AuditLog.ModuleName.SYSTEM, "INITIALIZE_SYSTEM", "ROOT", "System Engine",
                "Central Database initialized with Finance, HR, Procurement, Sales, Manufacturing, and Admin Security modules connected."));

        System.out.println(">>> Central Database initialization complete. ERP ready with Roles & Administration!");
    }
}
