package com.bank.dao;

import com.bank.model.Account;
import java.math.BigDecimal;
import java.sql.*;

public class AccountDAO {

    public int insertCustomer(Connection c, String name, String email, String phone) throws SQLException {
        String sql = "INSERT INTO customers (name, email, phone) VALUES (?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public long insertAccount(Connection c, int customerId, String type, BigDecimal balance, String pinHash)
            throws SQLException {
        String sql = "INSERT INTO accounts (customer_id, account_type, balance, pin_hash) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.setString(2, type);
            ps.setBigDecimal(3, balance);
            ps.setString(4, pinHash);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    /** Row-locks the account for the rest of the transaction so concurrent updates cannot corrupt the balance. */
    public Account findForUpdate(Connection c, long accountNo) throws SQLException {
        String sql = "SELECT account_no, customer_id, account_type, balance, pin_hash "
                   + "FROM accounts WHERE account_no = ? FOR UPDATE";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Account(rs.getLong("account_no"), rs.getInt("customer_id"),
                        rs.getString("account_type"), rs.getBigDecimal("balance"), rs.getString("pin_hash"));
            }
        }
    }

    public void updateBalance(Connection c, long accountNo, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_no = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setLong(2, accountNo);
            ps.executeUpdate();
        }
    }
}
