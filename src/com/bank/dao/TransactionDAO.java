package com.bank.dao;

import com.bank.model.Transaction;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public void insert(Connection c, long accountNo, String type, BigDecimal amount,
                       BigDecimal balanceAfter, Long relatedAccount) throws SQLException {
        String sql = "INSERT INTO transactions (account_no, txn_type, amount, balance_after, related_account) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, accountNo);
            ps.setString(2, type);
            ps.setBigDecimal(3, amount);
            ps.setBigDecimal(4, balanceAfter);
            if (relatedAccount == null) ps.setNull(5, Types.BIGINT);
            else ps.setLong(5, relatedAccount);
            ps.executeUpdate();
        }
    }

    public List<Transaction> findByAccount(Connection c, long accountNo, int limit) throws SQLException {
        String sql = "SELECT txn_id, account_no, txn_type, amount, balance_after, related_account, txn_time "
                   + "FROM transactions WHERE account_no = ? ORDER BY txn_time DESC, txn_id DESC LIMIT ?";
        List<Transaction> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, accountNo);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long rel = rs.getLong("related_account");
                    Long related = rs.wasNull() ? null : rel;
                    list.add(new Transaction(rs.getLong("txn_id"), rs.getLong("account_no"),
                            rs.getString("txn_type"), rs.getBigDecimal("amount"),
                            rs.getBigDecimal("balance_after"), related, rs.getTimestamp("txn_time")));
                }
            }
        }
        return list;
    }
}
