-- =====================================================================
-- Personal Finance & Monthly Expense Management System
-- SQLite schema (normalised, foreign keys enforced).
-- Monetary values are stored as INTEGER minor units (e.g. cents).
-- Dates are ISO-8601 TEXT: 'YYYY-MM-DD'; timestamps are ISO instants.
-- =====================================================================

PRAGMA foreign_keys = ON;

-- ---------------------------------------------------------------------
-- Master data
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS category (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    name      TEXT    NOT NULL,
    type      TEXT    NOT NULL CHECK (type IN ('EXPENSE', 'INCOME')),
    color_hex TEXT,
    icon      TEXT,
    archived  INTEGER NOT NULL DEFAULT 0 CHECK (archived IN (0, 1)),
    UNIQUE (name, type)
);

CREATE TABLE IF NOT EXISTS payment_method (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    name     TEXT    NOT NULL UNIQUE,
    type     TEXT    NOT NULL CHECK (type IN
             ('CASH','DEBIT_CARD','CREDIT_CARD','BANK_TRANSFER','E_WALLET','OTHER')),
    archived INTEGER NOT NULL DEFAULT 0 CHECK (archived IN (0, 1))
);

CREATE TABLE IF NOT EXISTS account (
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    name                 TEXT    NOT NULL UNIQUE,
    type                 TEXT    NOT NULL CHECK (type IN
                         ('CASH','CHECKING','SAVINGS','CREDIT_CARD','INVESTMENT','OTHER')),
    opening_balance_minor INTEGER NOT NULL DEFAULT 0,
    currency             TEXT    NOT NULL,
    archived             INTEGER NOT NULL DEFAULT 0 CHECK (archived IN (0, 1))
);

-- ---------------------------------------------------------------------
-- Transactions
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS expense (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id        INTEGER NOT NULL,
    category_id       INTEGER,
    payment_method_id INTEGER,
    amount_minor      INTEGER NOT NULL CHECK (amount_minor <= 0), -- expenses are negative
    currency          TEXT    NOT NULL,
    description       TEXT    NOT NULL DEFAULT '',
    txn_date          TEXT    NOT NULL,
    created_at        TEXT    NOT NULL,
    FOREIGN KEY (account_id)        REFERENCES account(id)        ON DELETE RESTRICT,
    FOREIGN KEY (category_id)       REFERENCES category(id)       ON DELETE SET NULL,
    FOREIGN KEY (payment_method_id) REFERENCES payment_method(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS income (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id  INTEGER NOT NULL,
    category_id INTEGER,
    amount_minor INTEGER NOT NULL CHECK (amount_minor >= 0), -- income is positive
    currency    TEXT    NOT NULL,
    description TEXT    NOT NULL DEFAULT '',
    txn_date    TEXT    NOT NULL,
    created_at  TEXT    NOT NULL,
    FOREIGN KEY (account_id)  REFERENCES account(id)  ON DELETE RESTRICT,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL
);

-- ---------------------------------------------------------------------
-- Budgeting & summaries
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS budget (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    category_id INTEGER NOT NULL,
    month       TEXT    NOT NULL,                         -- 'YYYY-MM'
    limit_minor INTEGER NOT NULL CHECK (limit_minor >= 0),
    currency    TEXT    NOT NULL,
    UNIQUE (category_id, month),
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS monthly_summary (
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    month                TEXT    NOT NULL UNIQUE,          -- 'YYYY-MM'
    total_income_minor   INTEGER NOT NULL,
    total_expense_minor  INTEGER NOT NULL,
    net_minor            INTEGER NOT NULL,
    savings_minor        INTEGER NOT NULL,
    outstanding_minor    INTEGER NOT NULL,
    currency             TEXT    NOT NULL,
    generated_at         TEXT    NOT NULL
);

-- ---------------------------------------------------------------------
-- Indexes for reporting hot paths
-- ---------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_expense_date        ON expense(txn_date);
CREATE INDEX IF NOT EXISTS idx_expense_category    ON expense(category_id);
CREATE INDEX IF NOT EXISTS idx_expense_account     ON expense(account_id);
CREATE INDEX IF NOT EXISTS idx_income_date         ON income(txn_date);
CREATE INDEX IF NOT EXISTS idx_income_category     ON income(category_id);
CREATE INDEX IF NOT EXISTS idx_income_account      ON income(account_id);
CREATE INDEX IF NOT EXISTS idx_budget_month        ON budget(month);
