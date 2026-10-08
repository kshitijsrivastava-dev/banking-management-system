package com.bank.exception;

public class AuthenticationException extends BankException {
    public AuthenticationException() { super("Incorrect PIN."); }
}
