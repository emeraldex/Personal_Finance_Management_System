# Deployment — Desktop (JavaFX)

## What "deployment" means here

The desktop app is a **local-first, single-user application**. There is no server,
no cloud backend and no database to provision — each user runs the app on their own
machine and it stores data in a local SQLite file. "Deploying" therefore means
**producing a native installer for each operating system and distributing it to
users**, not standing up a host.

The end result is a self-contained installer (`.msi`/`.exe` on Windows, `.dmg`/`.pkg`
on macOS, `.deb`/`.rpm` on Linux) that **bundles its own Java runtime**, so users do
not need Java installed.

---

## 1. Prerequisites

| Requirement | Notes |
|---|---|
| **JDK 21** | Provides `jpackage`, used to build the installers. Verify with `jpackage --version`. |
| **Maven 3.8+** | Builds the core and the shaded desktop jar. |
| **Per-OS packaging tool** | Windows: [WiX Toolset v3](https://wixtoolset.org/) on `PATH` (for `.msi`). macOS: Xcode command-line tools (for `.dmg`/`.pkg`). Linux: `dpkg`/`fakeroot` (for `.deb`) or `rpm-build` (for `.rpm`). |

**`jpackage` cannot cross-build.** A Windows installer must be built on Windows, a
macOS installer on macOS, and a Linux package on Linux. This is normally automated
with a CI build matrix (see §7).

---

## 2. Two one-time build additions

Because the desktop module is a **classpath (non-modular) application**, two small
additions are required before it can be packaged reliably. Add them once and commit.

### 2a. A launcher class

Launching a class that directly extends `javafx.application.Application` from a fat
jar triggers *"Error: JavaFX runtime components are missing"*. A plain launcher that
is **not** an `Application` subclass avoids this. Create:

`expense-desktop/src/main/java/com/expense/desktop/Launcher.java`
```java
package com.expense.desktop;

/**
 * Packaging entry point. Delegating to the JavaFX app from a non-Application
 * main class lets the shaded jar start without the JavaFX module path.
 */
public final class Launcher {
    public static void main(String[] args) {
        ExpenseDesktopApp.main(args);
    }
}
```

### 2b. The Shade plugin (build a fat jar)

Add the Maven Shade plugin to `expense-desktop/pom.xml` inside `<build><plugins>`.
It merges the core, JavaFX, `sqlite-jdbc` and Jackson into one runnable jar. The
`ServicesResourceTransformer` is essential — it merges the `META-INF/services`
files that the JDBC driver and Jackson rely on.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.6.0</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals><goal>shade</goal></goals>
            <configuration>
                <shadedArtifactAttached>true</shadedArtifactAttached>
                <shadedClassifierName>shaded</shadedClassifierName>
                <transformers>
                    <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                        <mainClass>com.expense.desktop.Launcher</mainClass>
                    </transformer>
                    <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                </transformers>
                <filters>
                    <filter>
                        <artifact>*:*</artifact>
                        <excludes>
                            <exclude>module-info.class</exclude>
                            <exclude>META-INF/*.SF</exclude>
                            <exclude>META-INF/*.DSA</exclude>
                            <exclude>META-INF/*.RSA</exclude>
                        </excludes>
                    </filter>
                </filters>
            </configuration>
        </execution>
    </executions>
</plugin>
```

> **JavaFX is platform-specific inside the fat jar.** JavaFX ships native binaries
> classified per OS, so a fat jar built on Linux contains Linux natives and runs on
> Linux only. Since you build each installer on its own OS anyway (`jpackage` is
> per-OS), this is not an extra burden — just build the jar on the same machine as
> the installer.

---

## 3. Build the artifacts

From the repository root:

```bash
# 1. Install the shared core into the local Maven repo
mvn -pl expense-core -am install

# 2. Build the shaded desktop jar
mvn -pl expense-desktop -am package
# -> expense-desktop/target/expense-desktop-1.0.0-shaded.jar
```

Stage the single shaded jar in a clean input directory so the installer bundles
only what it needs:

```bash
rm -rf dist && mkdir dist
cp expense-desktop/target/expense-desktop-1.0.0-shaded.jar dist/
```

Before packaging, smoke-test the jar:

```bash
java -jar dist/expense-desktop-1.0.0-shaded.jar
```

---

## 4. Build the installer with `jpackage`

`jpackage` bundles the runtime of the JDK it is run from, so the installed app is
fully self-contained. Run the command that matches your OS.

### Windows (run on Windows, WiX on `PATH`)
```powershell
jpackage `
  --type msi `
  --name "Expense Manager" `
  --app-version 1.0.0 `
  --vendor "Your Company" `
  --input dist `
  --main-jar expense-desktop-1.0.0-shaded.jar `
  --main-class com.expense.desktop.Launcher `
  --icon packaging/windows/app.ico `
  --win-menu --win-shortcut `
  --win-upgrade-uuid 8b0f6b1e-4c1a-4a5e-9f0a-000000000001 `
  --dest installers
```
Keep `--win-upgrade-uuid` **stable across releases** so new MSIs upgrade in place
instead of installing side by side. Use `--type exe` for an EXE installer instead.

### macOS (run on macOS)
```bash
jpackage \
  --type dmg \
  --name "Expense Manager" \
  --app-version 1.0.0 \
  --vendor "Your Company" \
  --input dist \
  --main-jar expense-desktop-1.0.0-shaded.jar \
  --main-class com.expense.desktop.Launcher \
  --icon packaging/macos/app.icns \
  --dest installers
```
Use `--type pkg` for a `.pkg`. For public distribution the app must be **signed and
notarized** (see §5), otherwise Gatekeeper blocks it.

### Linux (run on Linux)
```bash
jpackage \
  --type deb \
  --name expense-manager \
  --app-version 1.0.0 \
  --input dist \
  --main-jar expense-desktop-1.0.0-shaded.jar \
  --main-class com.expense.desktop.Launcher \
  --icon packaging/linux/app.png \
  --linux-shortcut \
  --dest installers
```
Use `--type rpm` on RPM-based distros (requires `rpm-build`).

Icons are optional; omit `--icon` to use the default. Recommended formats: `.ico`
(Windows), `.icns` (macOS), `.png` (Linux).

---

## 5. Code signing (required for real distribution)

Unsigned installers trigger security warnings and may be blocked outright.

- **Windows** — sign the produced `.msi`/`.exe` with Authenticode using
  `signtool sign /f cert.pfx /p <pwd> /tr <timestamp-url> /td sha256 /fd sha256 installers\*.msi`.
- **macOS** — sign during packaging with `--mac-sign` and
  `--mac-signing-key-user-name "Developer ID Application: Your Company (TEAMID)"`,
  then **notarize** the `.dmg` with `xcrun notarytool submit … --wait` and staple it
  with `xcrun stapler staple`.
- **Linux** — signing is distribution-specific (e.g. `dpkg-sig` / repository GPG
  signing); usually handled by the package repository rather than the file itself.

---

## 6. Runtime data, upgrades and uninstall

- **Where data lives.** On first launch the app creates its database at
  `~/.expense-manager/expenses.db` (`%USERPROFILE%\.expense-manager\expenses.db` on
  Windows). There is nothing to configure after install.
- **Backup / restore.** Because it is a single SQLite file, backup is a file copy;
  restore is copying it back while the app is closed.
- **Upgrades.** Bump `--app-version` for each release. On Windows the stable
  `--win-upgrade-uuid` makes the new MSI replace the old install. User data in
  `~/.expense-manager` is untouched by upgrades.
- **Uninstall.** Uninstalling removes the app but **not** the user's data directory,
  so reinstalling preserves history. Document this for users who want a clean wipe.

---

## 7. Optional: CI build matrix

Because installers are per-OS, a CI matrix produces all three from one tag. Sketch
for GitHub Actions:

```yaml
name: package-desktop
on:
  push:
    tags: ['v*']
jobs:
  build:
    strategy:
      matrix:
        os: [windows-latest, macos-latest, ubuntu-latest]
    runs-on: ${{ matrix.os }}
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '21' }
      - run: mvn -pl expense-core -am install -B
      - run: mvn -pl expense-desktop -am package -B
      # then stage dist/ and run the OS-appropriate jpackage command,
      # and upload installers/ as a release asset.
```

Store signing certificates/keys as encrypted CI secrets and inject them into the
signing steps — never commit them.

---

## 8. Troubleshooting

| Symptom | Cause & fix |
|---|---|
| `Error: JavaFX runtime components are missing` | The app was launched via the `Application` subclass. Package with `--main-class com.expense.desktop.Launcher` (§2a). |
| `No suitable driver` / SQLite driver not found at runtime | The `META-INF/services` files were not merged. Ensure the Shade `ServicesResourceTransformer` is present (§2b). |
| `jpackage: Cannot find tool: WiX` (Windows) | Install WiX Toolset v3 and ensure it is on `PATH`. |
| Installer builds but app window never appears | Run the shaded jar directly (`java -jar …`) to see the stack trace; usually a missing platform-classified JavaFX native (build the jar on the same OS as the installer). |
| Installer is very large | Expected — it bundles a full JDK runtime. To shrink it, modularize the app and pass a `jlink`-minimized `--runtime-image` (a later optimization). |
