CREATE TABLE Category (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL
);

CREATE TABLE PaymentMethod (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL
);

CREATE TABLE Account (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL
);

CREATE TABLE Budget (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    amount REAL NOT NULL
);

CREATE TABLE Expense (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date TEXT NOT NULL,
    description TEXT NOT NULL,
    amount REAL NOT NULL,
    category_id INTEGER,
    payment_method_id INTEGER,
    account_id INTEGER,
    FOREIGN KEY(category_id) REFERENCES Category(id),
    FOREIGN KEY(payment_method_id) REFERENCES PaymentMethod(id),
    FOREIGN KEY(account_id) REFERENCES Account(id)
);

CREATE TABLE Income (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date TEXT NOT NULL,
    description TEXT NOT NULL,
    amount REAL NOT NULL,
    category_id INTEGER,
    payment_method_id INTEGER,
    account_id INTEGER,
    FOREIGN KEY(category_id) REFERENCES Category(id),
    FOREIGN KEY(payment_method_id) REFERENCES PaymentMethod(id),
    FOREIGN KEY(account_id) REFERENCES Account(id)
);

CREATE TABLE MonthlySummary (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    month TEXT NOT NULL,
    total_income REAL NOT NULL,
    total_expenses REAL NOT NULL,
    net_balance REAL NOT NULL,
    savings REAL NOT NULL,
    outstanding REAL NOT NULL
);
