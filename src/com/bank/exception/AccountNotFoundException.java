package com.bank.exception;

public class AccountNotFoundException extends BankException {
    public AccountNotFoundException(long accountNo) {
        super("Account not found: " + accountNo);
    }
}
