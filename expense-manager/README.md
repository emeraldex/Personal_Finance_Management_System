# Personal Finance & Monthly Expense Management System

A modular personal-finance manager with a **UI-independent core** reused by a
JavaFX desktop app and a Jetpack Compose Android app.

## Modules

| Module            | Type                     | Status (iteration 1)                         |
|-------------------|--------------------------|----------------------------------------------|
| `expense-core`    | Pure Java 21 library     | **Complete & tested** (69 tests, all green)  |
| `expense-desktop` | JavaFX (MVVM)            | Dashboard (month paging, CSV/Excel/PDF export), Add Expense (auto-categorise, budget alerts, multi-currency entry), Add Income, History (edit/delete), Budgets, Manage (archive/rename), Settings (Excel import, DB backup, exchange rates) |
| `expense-android` | Android / Compose (MVVM) | Dashboard, Add Expense/Income (budget alerts), History (delete), Reports (budgets), Settings — bottom-nav |
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
│       ├── network/      # external-integration seams: sync, auth, OCR, AI, FX, bank feed, notifications
│       └── report/       # analytics DTOs + CSV/Excel/PDF exporters + import SPI
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

2. **Done.** Android now has `android.database.sqlite` implementations of the
   core repository ports (`Android*Repository`), so it runs on a real device with
   no JDBC driver; the screens (Dashboard, Add Expense/Income, History, Reports,
   Settings) ship with bottom navigation. On desktop: Income, History, Budgets and
   Settings screens, plus CSV/Excel/PDF export and Excel import via Apache POI /
   PDFBox. `ExpenseManager` gained a DI constructor so either persistence adapter
   (JDBC on desktop, SQLite on Android) plugs into the same services unchanged.
3. Remaining seams awaiting external infrastructure: cloud sync (`SyncClient`,
   needs a backend) and OCR receipt scanning (`ReceiptScanner`, needs an OCR
   engine); multi-user accounts build on cloud sync via `AuthClient` (needs an
   identity provider). One seam remains ready for wiring: `BankFeedClient`
   (automatic bank-transaction import). `ExchangeRateProvider` is implemented:
   the offline `FixedExchangeRateProvider` (rates maintained in desktop
   Settings) powers `CurrencyConversionService`, letting the desktop Add
   Expense form take amounts in a foreign currency and convert them to the app
   currency on save; a live rates API can replace the fixed table behind the
   same seam. `NotificationPublisher` is implemented on both
   front ends: the core `BudgetAlertService` decides when a budget deserves
   attention; delivery is a system-tray balloon on desktop (in-window toast
   fallback) and a "Budget alerts" notification channel on Android — each with
   a Settings toggle. The offline
   `HeuristicExpenseCategorizer` is now wired into the desktop Add-Expense form;
   an ML/LLM-backed categoriser can replace it behind the same seam.
