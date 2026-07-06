# Personal Finance & Monthly Expense Management System

## Project Structure

The project is divided into the following modules:

1. **expense-core**: Contains all business logic and is UI-independent.
2. **expense-desktop**: JavaFX-based desktop application.
3. **expense-android**: Android application using Jetpack Compose.
4. **documentation**: Contains project documentation.

## Database Schema

The SQLite database consists of the following tables:

- **Category**: Stores categories for expenses and incomes.
- **PaymentMethod**: Stores payment methods.
- **Account**: Stores accounts.
- **Budget**: Stores budget information.
- **Expense**: Stores expense records.
- **Income**: Stores income records.
- **MonthlySummary**: Stores monthly summary data.

## SQL Scripts

The SQL scripts to initialize the database are located in `expense-core/src/main/resources/schema.sql`.

## Java Source Code

The Java source code is organized into packages within each module. The `expense-core` module contains domain models, DTOs, repositories, services, and other necessary components.

## Unit Tests

Unit tests for the business services are located in the `src/test/java` directory of each module.

## Build Instructions

To build the project, run the following command in the root directory:

```
cd C:\Users\USER\OneDrive\Special Project\Personal_Finance_Management_System

To build the entire project:
```sh
mvn clean install```