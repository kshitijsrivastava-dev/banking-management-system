package com.bank.exception;

/** Base class for all business-rule errors in the banking app. */
public class BankException extends Exception {
    public BankException(String message) { super(message); }
    public BankException(String message, Throwable cause) { super(message, cause); }
}
