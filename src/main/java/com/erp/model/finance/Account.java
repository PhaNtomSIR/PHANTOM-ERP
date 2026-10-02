package com.erp.model.finance;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "chart_of_accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String accountCode; // e.g. "1010", "1020", "2010", "4010", "5010"

    @Column(nullable = false)
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type; // ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE

    private BigDecimal balance = BigDecimal.ZERO;
    private String description;

    public enum AccountType {
        ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
    }

    public Account() {}

    public Account(String accountCode, String accountName, AccountType type, BigDecimal balance, String description) {
        this.accountCode = accountCode;
        this.accountName = accountName;
        this.type = type;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public AccountType getType() { return type; }
    public void setType(AccountType type) { this.type = type; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
