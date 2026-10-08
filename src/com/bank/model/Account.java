package com.bank.model;

import java.math.BigDecimal;

public class Account {
    private final long accountNo;
    private final int customerId;
    private final String accountType;
    private BigDecimal balance;
    private final String pinHash;

    public Account(long accountNo, int customerId, String accountType, BigDecimal balance, String pinHash) {
        this.accountNo = accountNo;
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
        this.pinHash = pinHash;
    }

    public long getAccountNo() { return accountNo; }
    public int getCustomerId() { return customerId; }
    public String getAccountType() { return accountType; }
    public BigDecimal getBalance() { return balance; }
    public String getPinHash() { return pinHash; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
