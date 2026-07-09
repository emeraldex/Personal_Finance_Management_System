# Build & Run

## Prerequisites
- JDK 21
- Maven 3.8+ (for core + desktop)
- Android Studio / Gradle 8.5+ and Android SDK 34 (for the Android app)

## Core + Desktop (Maven reactor)

Build everything and run the core test suite:
```bash
mvn clean install
```

Run only the core tests:
```bash
mvn -pl expense-core test
```

Launch the desktop app (JavaFX plugin handles the JavaFX runtime/modules):
```bash
mvn -pl expense-desktop javafx:run
```
The desktop app creates its database at `~/.expense-manager/expenses.db`.

## Android app
```bash
cd expense-android
./gradlew :app:assembleDebug        # build APK
./gradlew :app:installDebug         # install on a connected device/emulator
```
> The Android module reuses `expense-core` for all business logic. Iteration 1
> ships the Compose UI, ViewModels, navigation, the `FinanceRepository` port,
> `CoreFinanceRepository` (delegates to the core), and `CoreProvider`/`MainActivity`
> wiring. The desktop-oriented `sqlite-jdbc` artifact bundles native binaries for
> desktop OSes, not Android ABIs, so on-device persistence requires either an
> Android-compatible SQLite-JDBC build or native-SQLite adapters implementing the
> core `*Repository` ports (iteration 2). The services and rules are identical
> either way — only the persistence adapter differs.

## Verifying the core in a restricted/offline environment
The core has only three runtime dependencies (sqlite-jdbc, Jackson) and JUnit 5
for tests. It can be compiled and tested with `javac` + the JUnit console
launcher when a Maven mirror is unavailable — see the CI notes in the repo.

## Notes on Excel/PDF
`report.WorkbookImporter` / `WorkbookExporter` and a PDF `ReportExporter` are
interfaces in iteration 1; their Apache POI / PDFBox implementations arrive in
iteration 2. CSV export (`MonthlySummaryCsvExporter`, `TransactionCsvExporter`)
is fully implemented and requires no extra dependencies.
