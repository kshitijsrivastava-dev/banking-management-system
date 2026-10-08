package com.bank.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaction {
    private final long txnId;
    private final long accountNo;
    private final String type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final Long relatedAccount;
    private final Timestamp time;

    public Transaction(long txnId, long accountNo, String type, BigDecimal amount,
                       BigDecimal balanceAfter, Long relatedAccount, Timestamp time) {
        this.txnId = txnId;
        this.accountNo = accountNo;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.relatedAccount = relatedAccount;
        this.time = time;
    }

    public long getTxnId() { return txnId; }
    public long getAccountNo() { return accountNo; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public Long getRelatedAccount() { return relatedAccount; }
    public Timestamp getTime() { return time; }
}
