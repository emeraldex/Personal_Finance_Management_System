# Deployment Summary — Expense Manager v1.0.0

**Date**: 2026-07-13  
**Status**: ✓ Successfully Deployed

---

## What Was Deployed

A **self-contained portable application** for Windows that includes:

- **Application**: Expense Manager JavaFX desktop client
- **Runtime**: Bundled JDK 21 (no external Java installation required)
- **Size**: 175.24 MB (compressed app-image)
- **Location**: `./installers/Expense Manager/`

---

## Deployment Artifacts

### Application Package Structure

```
installers/
└── Expense Manager/
    ├── Expense Manager.exe           # Main launcher executable
    ├── app/
    │   ├── expense-desktop-1.0.0-shaded.jar  # Application JAR
    │   └── Expense Manager.cfg       # Configuration file
    └── runtime/                      # Bundled JDK 21
        ├── bin/                      # JVM executables
        ├── lib/                      # JVM libraries
        └── ...                       # Full JDK runtime
```

### How to Run

1. Navigate to `./installers/Expense Manager/`
2. Double-click `Expense Manager.exe`
3. Application launches with no additional setup required

---

## Data Storage

- **Location**: `%USERPROFILE%\.expense-manager\`
- **Database**: `expenses.db` (SQLite)
- **Notes**: 
  - Created automatically on first launch
  - Survives application updates
  - Can be backed up by copying the file

---

## What Was Done During Deployment

### Phase 1: One-Time Build Setup ✓
- ✓ Created `Launcher.java` — Packaging entry point (non-Application class)
- ✓ Added Maven Shade plugin to `expense-desktop/pom.xml`
  - Merges core, JavaFX, SQLite JDBC, and Jackson into one fat JAR
  - Includes `ServicesResourceTransformer` for proper JDBC/Jackson metadata merging

### Phase 2: Build Artifacts ✓
- ✓ Installed `expense-core` module to local Maven repository
- ✓ Built `expense-desktop` module with Shade plugin
  - Output: `expense-desktop/target/expense-desktop-1.0.0-shaded.jar` (25.6 MB)
  - All 50 unit tests passed ✓

### Phase 3: Packaging ✓
- ✓ Created distribution directory and staged shaded JAR
- ✓ Smoke-tested JAR to verify functionality
- ✓ Built portable app-image using `jpackage`
  - Includes bundled JDK 21
  - Platform: Windows (x86_64)

### Phase 4: Verification ✓
- ✓ Tested deployed executable — launches successfully

---

## For Production Distribution (Next Steps)

### To Create a Windows MSI Installer

If distributing to end users, install **WiX Toolset v3** and run:

```powershell
jpackage `
  --type msi `
  --name "Expense Manager" `
  --app-version 1.0.0 `
  --vendor "Your Company" `
  --input dist `
  --main-jar expense-desktop-1.0.0-shaded.jar `
  --main-class com.expense.desktop.Launcher `
  --win-menu --win-shortcut `
  --win-upgrade-uuid 8b0f6b1e-4c1a-4a5e-9f0a-000000000001 `
  --dest installers
```

Keep `--win-upgrade-uuid` stable across releases for in-place upgrades.

### To Code-Sign the Installer (Required for Real Distribution)

```powershell
signtool sign /f cert.pfx /p <password> /tr <timestamp-url> /td sha256 /fd sha256 installers\*.msi
```

---

## Platform-Specific Builds

This build is for **Windows**. To deploy on other platforms, build the same artifacts on:

- **macOS**: Creates `.dmg` or `.pkg` (may require code signing + notarization)
- **Linux**: Creates `.deb` or `.rpm`

Use the exact same `jpackage` command with:
- `--type dmg` (macOS)
- `--type deb` or `--type rpm` (Linux)
- `--icon` pointing to platform-specific icon format

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| "Cannot find WiX tools" | Install WiX Toolset v3 from https://wixtoolset.org (for MSI/EXE) |
| App fails to launch | Ensure `%USERPROFILE%\.expense-manager` exists (created on first run) |
| Database not found error | Check file permissions in `%USERPROFILE%\.expense-manager` |
| "JavaFX runtime components missing" | Verify using `Launcher.java` as entry point (already configured) |

---

## Upgrade & Uninstall

- **Upgrades**: Bump `--app-version` and rebuild. User data in `~/.expense-manager` is preserved.
- **Uninstall**: Remove the application directory. Data in `~/.expense-manager` is **not** removed (by design).
- **Clean wipe**: Manually delete `%USERPROFILE%\.expense-manager` to reset database.

---

## File Checklist

- ✓ Launcher.java created
- ✓ Shade plugin added to pom.xml
- ✓ Shaded JAR built and tested
- ✓ Portable app-image created (175.24 MB)
- ✓ Executable tested and working

**Ready for distribution or further customization.**
