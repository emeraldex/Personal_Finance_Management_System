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
> The Android module reuses `expense-core` for all business logic. On-device
> persistence uses `android.database.sqlite` adapters (`data.Android*Repository`)
> implementing the core repository ports — no JDBC driver ships in the APK; the
> services and rules are identical to desktop, only the storage adapter differs.
> Build the core jar first (`mvn -pl expense-core -am install` from
> `expense-manager/`); the Gradle build consumes it by path. Robolectric suites
> exercise the SQLite adapters, the notification publisher and the FX/bank-feed
> repository paths against real Android SQLite on the JVM — no emulator needed.

## Continuous integration
GitHub Actions (`.github/workflows/ci.yml`) runs on every push: one job builds
core + desktop in a single Maven reactor pass (running the full core test
suite), the other validates the Gradle wrapper, builds the core jar, assembles
the debug APK and runs the Android unit tests.

## Verifying the core in a restricted/offline environment
The core's required runtime dependencies are sqlite-jdbc and Jackson
(databind + jsr310); Apache POI and PDFBox are optional and bundled by the
desktop app's shaded jar. The core can be compiled and tested with `javac` +
the JUnit console launcher when a Maven mirror is unavailable.

## Notes on Excel/PDF
`report.WorkbookImporter` / `WorkbookExporter` and the PDF `ReportExporter`
are implemented by `PoiWorkbookImporter` / `PoiWorkbookExporter` (Apache POI)
and `PdfSummaryExporter` (PDFBox); the desktop app bundles both libraries in
its shaded jar. CSV export (`MonthlySummaryCsvExporter`,
`TransactionCsvExporter`) and bank-statement CSV import
(`CsvStatementBankFeedClient`) require no extra dependencies.
