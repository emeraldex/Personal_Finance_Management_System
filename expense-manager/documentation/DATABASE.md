# Database

SQLite, normalised, foreign keys enforced (`PRAGMA foreign_keys = ON`).
Monetary values are stored as **INTEGER minor units** (e.g. cents); dates as
ISO-8601 TEXT. The full DDL lives in
`expense-core/src/main/resources/db/schema.sql` and is applied idempotently on
startup by `SchemaInitializer`.

## Entity–relationship diagram

```mermaid
erDiagram
    ACCOUNT ||--o{ EXPENSE : owns
    ACCOUNT ||--o{ INCOME : owns
    CATEGORY ||--o{ EXPENSE : classifies
    CATEGORY ||--o{ INCOME : classifies
    CATEGORY ||--o{ BUDGET : caps
    PAYMENT_METHOD ||--o{ EXPENSE : "paid via"

    ACCOUNT {
        INTEGER id PK
        TEXT name UK
        TEXT type
        INTEGER opening_balance_minor
        TEXT currency
        INTEGER archived
    }
    CATEGORY {
        INTEGER id PK
        TEXT name
        TEXT type "EXPENSE|INCOME"
        TEXT color_hex
        TEXT icon
        INTEGER archived
    }
    PAYMENT_METHOD {
        INTEGER id PK
        TEXT name UK
        TEXT type
        INTEGER archived
    }
    EXPENSE {
        INTEGER id PK
        INTEGER account_id FK
        INTEGER category_id FK
        INTEGER payment_method_id FK
        INTEGER amount_minor "CHECK <= 0"
        TEXT currency
        TEXT description
        TEXT txn_date
        TEXT created_at
    }
    INCOME {
        INTEGER id PK
        INTEGER account_id FK
        INTEGER category_id FK
        INTEGER amount_minor "CHECK >= 0"
        TEXT currency
        TEXT description
        TEXT txn_date
        TEXT created_at
    }
    BUDGET {
        INTEGER id PK
        INTEGER category_id FK
        TEXT month "YYYY-MM"
        INTEGER limit_minor "CHECK >= 0"
        TEXT currency
    }
    MONTHLY_SUMMARY {
        INTEGER id PK
        TEXT month UK
        INTEGER total_income_minor
        INTEGER total_expense_minor
        INTEGER net_minor
        INTEGER savings_minor
        INTEGER outstanding_minor
        TEXT currency
        TEXT generated_at
    }
```

## Referential-integrity rules

- `expense.account_id`, `income.account_id` → `account` **ON DELETE RESTRICT**
  (cannot delete an account that still owns transactions).
- `expense.category_id`, `income.category_id` → `category` **ON DELETE SET NULL**
  (deleting a category preserves transaction history as "Uncategorised").
- `budget.category_id` → `category` **ON DELETE CASCADE**.
- `monthly_summary` is a recomputable cache keyed uniquely by `month`.
