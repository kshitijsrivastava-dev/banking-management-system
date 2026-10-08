CREATE DATABASE IF NOT EXISTS bank_db;
USE bank_db;

CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    phone       VARCHAR(15)  NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts (
    account_no   BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id  INT NOT NULL,
    account_type ENUM('SAVINGS','CURRENT') NOT NULL,
    balance      DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    pin_hash     CHAR(64) NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_balance CHECK (balance >= 0),
    CONSTRAINT fk_acc_cust FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
) AUTO_INCREMENT = 1000100001;

CREATE TABLE IF NOT EXISTS transactions (
    txn_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_no      BIGINT NOT NULL,
    txn_type        ENUM('DEPOSIT','WITHDRAWAL','TRANSFER_IN','TRANSFER_OUT') NOT NULL,
    amount          DECIMAL(15,2) NOT NULL,
    balance_after   DECIMAL(15,2) NOT NULL,
    related_account BIGINT NULL,
    txn_time        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_txn_acc FOREIGN KEY (account_no) REFERENCES accounts(account_no)
);

CREATE INDEX idx_txn_account_time ON transactions(account_no, txn_time);
