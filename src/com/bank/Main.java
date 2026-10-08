package com.bank;

import com.bank.exception.BankException;
import com.bank.exception.InvalidInputException;
import com.bank.model.Transaction;
import com.bank.service.BankService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner in = new Scanner(System.in);
    private static final BankService bank = new BankService();

    public static void main(String[] args) {
        System.out.println("=== Banking Management System ===");
        while (true) {
            System.out.println("\n1. Create Account\n2. Deposit\n3. Withdraw\n4. Transfer\n5. Check Balance\n6. Mini Statement\n7. Exit");
            try {
                int choice = Integer.parseInt(ask("Choose an option: ").trim());
                switch (choice) {
                    case 1 -> create();
                    case 2 -> deposit();
                    case 3 -> withdraw();
                    case 4 -> transfer();
                    case 5 -> balance();
                    case 6 -> statement();
                    case 7 -> { System.out.println("Thank you. Goodbye!"); return; }
                    default -> System.out.println("Invalid option.");
                }
            } catch (BankException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error: please enter a valid number.");
            }
        }
    }

    private static void create() throws BankException {
        String name = ask("Full name: ");
        String email = ask("Email: ");
        String phone = ask("Phone (10 digits): ");
        String type = ask("Account type (SAVINGS/CURRENT): ").trim().toUpperCase();
        BigDecimal initial = amount("Opening deposit: ");
        String pin = ask("Set 4-digit PIN: ");
        long accNo = bank.createAccount(name, email, phone, type, initial, pin);
        System.out.println("Account created. Your account number is " + accNo);
    }

    private static void deposit() throws BankException {
        long acc = Long.parseLong(ask("Account number: ").trim());
        BigDecimal bal = bank.deposit(acc, amount("Amount: "));
        System.out.println("Deposit successful. New balance: " + bal);
    }

    private static void withdraw() throws BankException {
        long acc = Long.parseLong(ask("Account number: ").trim());
        BigDecimal amt = amount("Amount: ");
        BigDecimal bal = bank.withdraw(acc, amt, ask("PIN: "));
        System.out.println("Withdrawal successful. New balance: " + bal);
    }

    private static void transfer() throws BankException {
        long from = Long.parseLong(ask("From account: ").trim());
        long to = Long.parseLong(ask("To account: ").trim());
        BigDecimal amt = amount("Amount: ");
        bank.transfer(from, to, amt, ask("PIN: "));
        System.out.println("Transfer successful.");
    }

    private static void balance() throws BankException {
        long acc = Long.parseLong(ask("Account number: ").trim());
        System.out.println("Balance: " + bank.getBalance(acc, ask("PIN: ")));
    }

    private static void statement() throws BankException {
        long acc = Long.parseLong(ask("Account number: ").trim());
        List<Transaction> list = bank.getStatement(acc, ask("PIN: "), 10);
        System.out.printf("%-8s %-14s %-12s %-12s %-12s %s%n", "TxnID", "Type", "Amount", "Balance", "Other A/C", "Time");
        for (Transaction t : list) {
            System.out.printf("%-8d %-14s %-12s %-12s %-12s %s%n", t.getTxnId(), t.getType(), t.getAmount(),
                    t.getBalanceAfter(), t.getRelatedAccount() == null ? "-" : t.getRelatedAccount(), t.getTime());
        }
    }

    private static BigDecimal amount(String prompt) throws BankException {
        try {
            return new BigDecimal(ask(prompt).trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Amount must be a number.");
        }
    }

    private static String ask(String prompt) {
        System.out.print(prompt);
        return in.nextLine();
    }
}
