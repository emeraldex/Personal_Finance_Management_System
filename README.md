# Personal Finance & Monthly Expense Management System

A modular personal-finance manager with a **UI-independent core** reused by a
JavaFX desktop app and a Jetpack Compose Android app.

## Modules

| Module            | Type                     | Status (iteration 1)                         |
|-------------------|--------------------------|----------------------------------------------|
| `expense-core`    | Pure Java 21 library     | **Complete & tested** (50 tests, all green)  |
| `expense-desktop` | JavaFX (MVVM)            | Dashboard + Add-Expense screens              |
| `expense-android` | Android / Compose (MVVM) | Dashboard + Quick-Expense screens            |
| `documentation`   | Docs                     | Architecture, ERD, build guide               |

The core contains **all** business logic (domain, validation, persistence ports,
services, analytics, reporting) and depends on no UI framework, so both front
ends consume it unchanged.

## Folder structure

```
expense-manager/
├── pom.xml                      # parent (reactor: expense-core, expense-desktop)
├── expense-core/
│   └── src/main/java/com/expense/core/
│       ├── domain/       # immutable entities + sealed Transaction, enums
│       ├── dto/          # request commands
│       ├── repository/   # ports (interfaces) + JDBC/SQLite implementations
│       ├── service/      # business services + ExpenseManager composition root
│       ├── database/     # Database, ConnectionProvider, SchemaInitializer
│       ├── mapper/       # ResultSet -> domain mapping
│       ├── util/         # Money value type
│       ├── validation/   # ValidationErrors / Validator
│       ├── exception/    # domain exception hierarchy
│       ├── network/      # cloud-sync / OCR / AI-categorisation seams
│       └── report/       # analytics DTOs + CSV exporters + Excel/PDF SPI
├── expense-desktop/
│   └── src/main/java/com/expense/desktop/
│       ├── ui/           # Views (scene-graph builders)
│       ├── viewmodel/    # ViewModels (observable, headless-testable)
│       └── AppContext, ExpenseDesktopApp
├── expense-android/      # Gradle project (Compose)
│   └── app/src/main/java/com/expense/android/
│       ├── ui/ navigation/ viewmodel/ repository/
└── documentation/
```

## Business rules enforced by the core

- Expenses are stored as **negative** amounts; income as **positive** amounts
  (enforced in the domain constructors *and* by DB `CHECK` constraints).
- Monthly summary computes: total income, total expenses, net balance, savings,
  outstanding, category breakdown, payment-method breakdown, cash flow and
  budget utilisation.
- Money uses `BigDecimal` + minor-unit integer storage — no floating-point drift.

See `documentation/` for the full architecture, ERD and build guide.

## Roadmap (subsequent iterations)

2. Android-SQLite implementations of the core repository ports; Excel import
   (legacy monthly workbook) + Excel/PDF export via Apache POI / PDFBox;
   remaining desktop screens (Income, Reports, Settings) and Android screens
   (History, Reports, Settings).
3. Cloud sync (`SyncClient`), OCR receipt scanning (`ReceiptScanner`),
   ML-backed categorisation (replacing `HeuristicExpenseCategorizer`),
   multi-user accounts. All plug into existing seams — no architectural change.
