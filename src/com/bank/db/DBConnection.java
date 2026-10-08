package com.bank.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Central place to obtain JDBC connections. Override defaults with env vars DB_URL, DB_USER, DB_PASS. */
public final class DBConnection {
    private static final String URL  = env("DB_URL",  "jdbc:mysql://localhost:3306/bank_db");
    private static final String USER = env("DB_USER", "root");
    private static final String PASS = env("DB_PASS", "root");

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? fallback : v;
    }
}
