package com.erp.service;

import com.erp.model.audit.AuditLog;
import com.erp.model.finance.Account;
import com.erp.model.finance.Invoice;
import com.erp.model.finance.JournalEntry;
import com.erp.model.finance.JournalLine;
import com.erp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinanceService {

    private final AccountRepository accountRepo;
    private final JournalEntryRepository journalRepo;
    private final InvoiceRepository invoiceRepo;
    private final AuditLogRepository auditRepo;

    public FinanceService(AccountRepository accountRepo,
                          JournalEntryRepository journalRepo,
                          InvoiceRepository invoiceRepo,
                          AuditLogRepository auditRepo) {
        this.accountRepo = accountRepo;
        this.journalRepo = journalRepo;
        this.invoiceRepo = invoiceRepo;
        this.auditRepo = auditRepo;
    }

    public List<Account> getAllAccounts() {
        return accountRepo.findAll();
    }

    public Account getAccountByCode(String code) {
        return accountRepo.findByAccountCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with code: " + code));
    }

    public Account createAccount(Account account) {
        Account saved = accountRepo.save(account);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.FINANCE, "CREATE_ACCOUNT",
                saved.getAccountCode(), "Finance Admin", "Created account " + saved.getAccountName()));
        return saved;
    }

    public List<JournalEntry> getAllJournalEntries() {
        return journalRepo.findTop50ByOrderByEntryDateDesc();
    }

    @Transactional
    public JournalEntry createJournalEntry(JournalEntry entry) {
        if (!entry.isBalanced()) {
            throw new IllegalArgumentException("Journal Entry must be balanced! Debit: "
                    + entry.getTotalDebit() + ", Credit: " + entry.getTotalCredit());
        }

        // Apply debits and credits to account balances
        for (JournalLine line : entry.getLines()) {
            Account acc = line.getAccount();
            BigDecimal debit = line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO;
            BigDecimal credit = line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO;

            // ASSET & EXPENSE accounts increase with Debit, decrease with Credit
            // LIABILITY, EQUITY & REVENUE increase with Credit, decrease with Debit
            if (acc.getType() == Account.AccountType.ASSET || acc.getType() == Account.AccountType.EXPENSE) {
                acc.setBalance(acc.getBalance().add(debit).subtract(credit));
            } else {
                acc.setBalance(acc.getBalance().add(credit).subtract(debit));
            }
            accountRepo.save(acc);
        }

        JournalEntry saved = journalRepo.save(entry);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.FINANCE, "POST_JOURNAL_ENTRY",
                saved.getEntryNumber(), "System / Finance", "Posted JE for " + saved.getDescription()));
        return saved;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepo.findAll();
    }

    @Transactional
    public Invoice recordPayment(Long invoiceId, BigDecimal paymentAmount) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        BigDecimal newPaid = invoice.getPaidAmount().add(paymentAmount);
        invoice.setPaidAmount(newPaid);
        if (newPaid.compareTo(invoice.getAmount()) >= 0) {
            invoice.setStatus(Invoice.InvoiceStatus.PAID);
        } else {
            invoice.setStatus(Invoice.InvoiceStatus.PARTIALLY_PAID);
        }

        // Post Journal Entry for Cash collection or payment
        Account cashAcc = getAccountByCode("1010");
        String jeNumber = "JE-PAY-" + System.currentTimeMillis() % 100000;
        JournalEntry je;

        if (invoice.getType() == Invoice.InvoiceType.AR_SALES) {
            // Debit Cash, Credit AR
            Account arAcc = getAccountByCode("1020");
            je = new JournalEntry(jeNumber, LocalDate.now(), "Customer Payment for " + invoice.getInvoiceNumber(), "FINANCE_AR", invoice.getInvoiceNumber());
            je.addLine(new JournalLine(cashAcc, paymentAmount, BigDecimal.ZERO, "Cash received"));
            je.addLine(new JournalLine(arAcc, BigDecimal.ZERO, paymentAmount, "Reduce AR"));
        } else {
            // AP Payment: Debit AP, Credit Cash
            Account apAcc = getAccountByCode("2010");
            je = new JournalEntry(jeNumber, LocalDate.now(), "Vendor Payment for " + invoice.getInvoiceNumber(), "FINANCE_AP", invoice.getInvoiceNumber());
            je.addLine(new JournalLine(apAcc, paymentAmount, BigDecimal.ZERO, "Reduce AP"));
            je.addLine(new JournalLine(cashAcc, BigDecimal.ZERO, paymentAmount, "Cash paid"));
        }

        createJournalEntry(je);
        Invoice saved = invoiceRepo.save(invoice);
        auditRepo.save(new AuditLog(AuditLog.ModuleName.FINANCE, "PAYMENT_RECORDED",
                saved.getInvoiceNumber(), "Cashier", "Payment of $" + paymentAmount + " on " + saved.getInvoiceNumber()));
        return saved;
    }

    public Map<String, Object> getFinancialSummary() {
        List<Account> accounts = accountRepo.findAll();

        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Account a : accounts) {
            switch (a.getType()) {
                case ASSET -> totalAssets = totalAssets.add(a.getBalance());
                case LIABILITY -> totalLiabilities = totalLiabilities.add(a.getBalance());
                case EQUITY -> totalEquity = totalEquity.add(a.getBalance());
                case REVENUE -> totalRevenue = totalRevenue.add(a.getBalance());
                case EXPENSE -> totalExpenses = totalExpenses.add(a.getBalance());
            }
        }

        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAssets", totalAssets);
        summary.put("totalLiabilities", totalLiabilities);
        summary.put("totalEquity", totalEquity);
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalExpenses", totalExpenses);
        summary.put("netProfit", netProfit);
        return summary;
    }
}
