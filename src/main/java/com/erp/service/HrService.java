package com.erp.service;

import com.erp.model.audit.AuditLog;
import com.erp.model.finance.Account;
import com.erp.model.finance.JournalEntry;
import com.erp.model.finance.JournalLine;
import com.erp.model.hr.Department;
import com.erp.model.hr.Employee;
import com.erp.model.hr.JobRequisition;
import com.erp.model.hr.LeaveRequest;
import com.erp.model.hr.Payroll;
import com.erp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class HrService {

    private final DepartmentRepository deptRepo;
    private final EmployeeRepository empRepo;
    private final PayrollRepository payrollRepo;
    private final LeaveRequestRepository leaveRepo;
    private final JobRequisitionRepository reqRepo;
    private final FinanceService financeService;
    private final AuditLogRepository auditRepo;

    public HrService(DepartmentRepository deptRepo,
                     EmployeeRepository empRepo,
                     PayrollRepository payrollRepo,
                     LeaveRequestRepository leaveRepo,
                     JobRequisitionRepository reqRepo,
                     FinanceService financeService,
                     AuditLogRepository auditRepo) {
        this.deptRepo = deptRepo;
        this.empRepo = empRepo;
        this.payrollRepo = payrollRepo;
        this.leaveRepo = leaveRepo;
        this.reqRepo = reqRepo;
        this.financeService = financeService;
        this.auditRepo = auditRepo;
    }

    public List<Department> getAllDepartments() {
        return deptRepo.findAll();
    }

    public Department saveDepartment(Department dept) {
        return deptRepo.save(dept);
    }

    public List<Employee> getAllEmployees() {
        return empRepo.findAll();
    }

    public Employee saveEmployee(Employee emp) {
        boolean isNew = emp.getId() == null;
        Employee saved = empRepo.save(emp);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.HR,
                isNew ? "HIRE_EMPLOYEE" : "UPDATE_EMPLOYEE",
                saved.getEmployeeCode(), "HR Officer",
                "Employee record: " + saved.getFullName() + " (" + saved.getPosition() + ")"));
        return saved;
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRepo.findAll();
    }

    public LeaveRequest saveLeaveRequest(LeaveRequest request) {
        return leaveRepo.save(request);
    }

    public LeaveRequest updateLeaveStatus(Long leaveId, LeaveRequest.LeaveStatus status) {
        LeaveRequest req = leaveRepo.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found: " + leaveId));
        req.setStatus(status);
        LeaveRequest saved = leaveRepo.save(req);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.HR, "LEAVE_STATUS_CHANGE",
                "LEAVE-" + saved.getId(), "HR Manager",
                "Leave request for " + saved.getEmployee().getFullName() + " set to " + status));
        return saved;
    }

    public List<Payroll> getAllPayrolls() {
        return payrollRepo.findAll();
    }

    @Transactional
    public List<Payroll> runMonthlyPayroll(String payPeriod) {
        List<Employee> activeEmployees = empRepo.findAll().stream()
                .filter(e -> e.getStatus() == Employee.EmployeeStatus.ACTIVE)
                .toList();

        List<Payroll> generatedPayrolls = new ArrayList<>();
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;

        for (Employee emp : activeEmployees) {
            BigDecimal monthlyGross = emp.getSalary() != null ?
                    emp.getSalary().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal tax = monthlyGross.multiply(BigDecimal.valueOf(0.15)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal deductions = BigDecimal.ZERO;
            BigDecimal net = monthlyGross.subtract(tax).subtract(deductions);

            String payrollNumber = "PAY-" + payPeriod + "-" + emp.getEmployeeCode();
            Payroll payroll = new Payroll(payrollNumber, emp, payPeriod, monthlyGross, deductions, tax, net, LocalDate.now());
            generatedPayrolls.add(payrollRepo.save(payroll));

            totalGross = totalGross.add(monthlyGross);
            totalTax = totalTax.add(tax);
            totalNet = totalNet.add(net);
        }

        // Post Journal Entry to General Ledger:
        // Debit Salaries Expense (5020) with total Gross
        // Credit Cash/Bank (1010) with total Net
        // Credit Tax / Payroll Liabilities (2020) with total Tax
        if (totalGross.compareTo(BigDecimal.ZERO) > 0) {
            Account salExpAcc = financeService.getAccountByCode("5020");
            Account cashAcc = financeService.getAccountByCode("1010");
            Account taxLiabAcc = financeService.getAccountByCode("2020");

            JournalEntry je = new JournalEntry(
                    "JE-PAYROLL-" + payPeriod,
                    LocalDate.now(),
                    "Payroll Distribution for Period " + payPeriod,
                    "PAYROLL",
                    payPeriod
            );
            je.addLine(new JournalLine(salExpAcc, totalGross, BigDecimal.ZERO, "Total Employee Gross Salaries"));
            je.addLine(new JournalLine(cashAcc, BigDecimal.ZERO, totalNet, "Net Salaries Paid out via Bank"));
            je.addLine(new JournalLine(taxLiabAcc, BigDecimal.ZERO, totalTax, "Withholding Tax Payable"));
            financeService.createJournalEntry(je);
        }

        auditRepo.save(new AuditLog(AuditLog.ModuleName.HR, "EXECUTE_PAYROLL",
                "PAYROLL-" + payPeriod, "HR / Finance Director",
                "Processed payroll for " + activeEmployees.size() + " employees. Total Gross: $" + totalGross));

        return generatedPayrolls;
    }

    public List<JobRequisition> getAllJobRequisitions() {
        return reqRepo.findAll();
    }

    public JobRequisition getJobRequisitionById(Long id) {
        return reqRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job Requisition not found: " + id));
    }

    public JobRequisition saveJobRequisition(JobRequisition req) {
        boolean isNew = req.getId() == null;
        if (isNew && req.getRequestedDate() == null) {
            req.setRequestedDate(LocalDate.now());
        }
        JobRequisition saved = reqRepo.save(req);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.HR,
                isNew ? "CREATE_JOB_REQUISITION" : "UPDATE_JOB_REQUISITION",
                saved.getRequisitionNumber(), "Hiring Manager",
                "Requisition for " + saved.getJobTitle() + " in " + (saved.getDepartment() != null ? saved.getDepartment().getName() : "N/A") + " (Openings: " + saved.getOpeningsCount() + ")"));
        return saved;
    }

    public JobRequisition updateRequisitionStatus(Long id, JobRequisition.RequisitionStatus status) {
        JobRequisition req = getJobRequisitionById(id);
        req.setStatus(status);
        JobRequisition saved = reqRepo.save(req);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.HR, "REQUISITION_STATUS_CHANGE",
                saved.getRequisitionNumber(), "HR Director",
                "Job requisition for " + saved.getJobTitle() + " status updated to " + status));
        return saved;
    }
}
