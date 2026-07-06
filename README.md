# Personal Finance & Monthly Expense Management System

## Overview

This system is a modular, production-quality application designed to manage personal finances and monthly expenses. It is built using Java 21, Maven, SQLite, JDBC, Jackson, JUnit 5, JavaFX, and Jetpack Compose (Android).

## Features

- **Expense CRUD**: Create, read, update, and delete expenses.
- **Income CRUD**: Create, read, update, and delete incomes.
- **Category Management**: Manage expense and income categories.
- **Payment Methods**: Manage payment methods for expenses and incomes.
- **Monthly Summary**: View a summary of monthly income, expenses, net balance, savings, outstanding, category breakdown, payment method breakdown, cash flow, and budget utilization.
- **Savings Tracking**: Track savings over time.
- **Outstanding Tracking**: Track outstanding expenses and incomes.
- **Dashboard Analytics**: View analytics and insights on the dashboard.
- **Budget Management**: Manage budgets and track budget utilization.
- **Reports**: Generate and view reports.
- **Excel Import**: Import legacy Excel workbooks matching the existing monthly expense format.
- **Excel Export**: Export reports back to Excel.
- **CSV Export**: Export reports to CSV.
- **PDF Export**: Export reports to PDF.
- **Charts**: View charts and graphs for expenses and incomes.
- **Cloud Synchronization**: Sync data between devices using Firebase.
- **OCR Receipt Scanning**: Scan receipts and extract expense information using Tesseract.
- **AI-Powered Expense Categorization**: Automatically categorize expenses using a TensorFlow model.
- **Multi-User Support**: Support multiple users with Firebase Authentication.

## Getting Started

### Prerequisites

- Java 21
- Maven
- SQLite
- Tesseract (for OCR receipt scanning)
- TensorFlow (for AI-powered expense categorization)
- Firebase (for cloud synchronization and multi-user support)

### Installation

1. Clone the repository:
   ```sh
   git clone https://github.com/your-repo/Personal_Finance_Management_System.git
   ```
2. Build the project:
   ```sh
   cd Personal_Finance_Management_System
   mvn clean install
   ```
3. Run the `expense-desktop` application:
   ```sh
   cd expense-desktop
   mvn javafx:run
   ```
4. Run the `expense-android` application:
   ```sh
   cd expense-android
  ./gradlew installDebug
   ```

## Usage

- Use the `expense-desktop` application to manage your finances on your desktop.
- Use the `expense-android` application to manage your finances on your Android device.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.