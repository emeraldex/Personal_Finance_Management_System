# Test Results — expense-core

All business logic in `expense-core` is covered by unit and integration
tests that run against a real in-memory SQLite database (JDBC). The suite was
compiled with `javac` (JDK 21) and executed with the JUnit 5 console launcher.

**50 tests across 11 test classes — all passing.**

### BudgetServiceTest
- Upsert Replaces Existing Budget For Same Category And Month
- Sets Budget For Expense Category
- List For Month Returns Only That Month
- Rejects Budget On Income Category

### CategoryServiceTest
- Rejects Blank Name
- List By Type Filters Correctly
- Rename Changes Name
- Archive Hides From Default But Keeps Row
- Get Unknown Throws
- Allows Same Name For Different Type
- Rejects Duplicate Name Of Same Type Case Insensitive

### CsvExporterTest
- Exports Monthly Summary As Csv
- Escapes Fields Containing Commas

### ExpenseServiceTest
- Rejects Unknown Account
- Create Stores Expense As Negative Amount
- Rejects Zero Amount
- Rejects Income Category On Expense
- Delete Removes Expense
- Update Changes Amount Keeping Negative Sign

### ForeignKeyIntegrationTest
- Check Constraint Rejects Positive Expense At Db Level
- Deleting Account With Transactions Is Rejected By Restrict
- Deleting Category Nulls Transaction Category Via On Delete Set Null

### HeuristicExpenseCategorizerTest
- Matches Category By Keyword
- Returns Empty For Blank Description
- Returns Empty When Nothing Matches

### IncomeServiceTest
- Rejects Expense Category On Income
- Create Stores Income As Positive Amount

### MoneyTest
- Rounds Half Even To Currency Scale
- Rejects Currency Mismatch
- Multiplies By Factor
- Adds And Subtracts Without Floating Point Drift
- Equality Ignores Trailing Zeros
- Converts To And From Minor Units
- Reports Sign Correctly

### MonthlySummaryServiceTest
- Outstanding Is Shortfall When Overspent
- Computes Headline Figures With Correct Signs
- Cash Flow Has One Point Per Active Day
- Category Breakdown Is Ranked By Magnitude With Percentages
- Empty Month Yields Zeroes In Default Currency
- Summarize And Cache Persists Snapshot
- Payment Breakdown Aggregates By Single Method
- Budget Utilization Flags Overspend

### PaymentMethodAndAccountServiceTest
- Creates Account With Opening Balance
- Archives Payment Method
- Rejects Duplicate Account Name
- Creates Payment Method
- Rejects Duplicate Payment Method Name
- Delete Unknown Account Throws


### TransactionSignTest
- Income Is Always Stored Positive Regardless Of Input Sign
- Expense Is Always Stored Negative Regardless Of Input Sign

## How to reproduce

```bash
mvn -pl expense-core test
```

The core depends only on sqlite-jdbc and Jackson at runtime (plus JUnit 5 for
tests), so it can also be compiled and tested with `javac` + the JUnit console
launcher in an offline environment without a Maven mirror.
