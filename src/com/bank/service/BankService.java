package com.bank.service;

import com.bank.dao.AccountDAO;
import com.bank.dao.TransactionDAO;
import com.bank.db.DBConnection;
import com.bank.exception.*;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.util.Validator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Business logic layer. Every money-moving operation runs inside one JDBC transaction:
 * commit on success, rollback on any failure.
 */
public class BankService {
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO txnDAO = new TransactionDAO();

    public long createAccount(String name, String email, String phone, String type,
                              BigDecimal initialDeposit, String pin) throws BankException {
        Validator.validateName(name);
        Validator.validateEmail(email);
        Validator.validatePhone(phone);
        Validator.validatePin(pin);
        if (!type.equals("SAVINGS") && !type.equals("CURRENT"))
            throw new InvalidInputException("Account type must be SAVINGS or CURRENT.");
        if (initialDeposit == null || initialDeposit.signum() < 0)
            throw new InvalidInputException("Initial deposit cannot be negative.");
        if (initialDeposit.scale() > 2)
            throw new InvalidInputException("Amount can have at most 2 decimal places.");
        BigDecimal minimum = type.equals("SAVINGS") ? new BigDecimal("500") : new BigDecimal("1000");
        if (initialDeposit.compareTo(minimum) < 0)
            throw new InvalidInputException("Minimum opening balance for " + type + " is " + minimum);

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int customerId = accountDAO.insertCustomer(c, name.trim(), email.trim().toLowerCase(), phone.trim());
                long accNo = accountDAO.insertAccount(c, customerId, type, initialDeposit, Validator.hashPin(pin));
                txnDAO.insert(c, accNo, "DEPOSIT", initialDeposit, initialDeposit, null);
                c.commit();
                return accNo;
            } catch (SQLException e) {
                c.rollback();
                if (e.getErrorCode() == 1062)
                    throw new InvalidInputException("A customer with this email already exists.");
                throw e;
            }
        } catch (SQLException e) {
            throw new BankException("Database error while creating account: " + e.getMessage(), e);
        }
    }

    public BigDecimal deposit(long accountNo, BigDecimal amount) throws BankException {
        Validator.validateAmount(amount);
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                Account acc = lock(c, accountNo);
                BigDecimal newBal = acc.getBalance().add(amount);
                accountDAO.updateBalance(c, accountNo, newBal);
                txnDAO.insert(c, accountNo, "DEPOSIT", amount, newBal, null);
                c.commit();
                return newBal;
            } catch (SQLException | BankException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BankException("Database error during deposit: " + e.getMessage(), e);
        }
    }

    public BigDecimal withdraw(long accountNo, BigDecimal amount, String pin) throws BankException {
        Validator.validateAmount(amount);
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                Account acc = lock(c, accountNo);
                authenticate(acc, pin);
                if (acc.getBalance().compareTo(amount) < 0)
                    throw new InsufficientFundsException("Insufficient balance. Available: " + acc.getBalance());
                BigDecimal newBal = acc.getBalance().subtract(amount);
                accountDAO.updateBalance(c, accountNo, newBal);
                txnDAO.insert(c, accountNo, "WITHDRAWAL", amount, newBal, null);
                c.commit();
                return newBal;
            } catch (SQLException | BankException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BankException("Database error during withdrawal: " + e.getMessage(), e);
        }
    }

    public void transfer(long fromAcc, long toAcc, BigDecimal amount, String pin) throws BankException {
        Validator.validateAmount(amount);
        if (fromAcc == toAcc) throw new InvalidInputException("Cannot transfer to the same account.");
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                // Lock in a fixed order (lowest account number first) to avoid deadlocks.
                Account first = lock(c, Math.min(fromAcc, toAcc));
                Account second = lock(c, Math.max(fromAcc, toAcc));
                Account from = (first.getAccountNo() == fromAcc) ? first : second;
                Account to = (from == first) ? second : first;

                authenticate(from, pin);
                if (from.getBalance().compareTo(amount) < 0)
                    throw new InsufficientFundsException("Insufficient balance. Available: " + from.getBalance());

                BigDecimal fromNew = from.getBalance().subtract(amount);
                BigDecimal toNew = to.getBalance().add(amount);
                accountDAO.updateBalance(c, fromAcc, fromNew);
                accountDAO.updateBalance(c, toAcc, toNew);
                txnDAO.insert(c, fromAcc, "TRANSFER_OUT", amount, fromNew, toAcc);
                txnDAO.insert(c, toAcc, "TRANSFER_IN", amount, toNew, fromAcc);
                c.commit();
            } catch (SQLException | BankException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BankException("Database error during transfer: " + e.getMessage(), e);
        }
    }

    public BigDecimal getBalance(long accountNo, String pin) throws BankException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            Account acc = lock(c, accountNo);
            authenticate(acc, pin);
            c.commit();
            return acc.getBalance();
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }

    public List<Transaction> getStatement(long accountNo, String pin, int limit) throws BankException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            Account acc = lock(c, accountNo);
            authenticate(acc, pin);
            List<Transaction> list = txnDAO.findByAccount(c, accountNo, limit);
            c.commit();
            return list;
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }

    private Account lock(Connection c, long accountNo) throws SQLException, AccountNotFoundException {
        Account acc = accountDAO.findForUpdate(c, accountNo);
        if (acc == null) throw new AccountNotFoundException(accountNo);
        return acc;
    }

    private void authenticate(Account acc, String pin) throws BankException {
        Validator.validatePin(pin);
        if (!acc.getPinHash().equals(Validator.hashPin(pin))) throw new AuthenticationException();
    }
}
