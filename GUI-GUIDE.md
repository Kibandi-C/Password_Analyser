# Swing application usage guide

## 1. GUI overview

The **Password Strength Analyzer** window uses standard Swing controls and layout managers. It contains a masked password field, a Show password checkbox, Analyze and Clear buttons, a prominent strength label and score bar, six validation statuses, a predictability indicator, and a read-only suggestion area.

The window starts at 660 by 720 pixels and can be resized. Scroll panes keep content reachable when space is limited. Strength is expressed in text as well as color. The password label identifies the input, buttons support keyboard mnemonics, and Enter activates Analyze.

## 2. Project structure

```text
src/com/sit300/password/component/       Independent component source
    PasswordAnalyzer.java
    PasswordResult.java
app-src/com/sit300/password/
    app/Main.java                       Desktop entry point
    ui/PasswordCheckerUI.java            Swing presentation and events
examples/com/sit300/password/demo/       Existing console consumer
tests/com/sit300/password/tests/         Existing component checks
dist/password-analyzer.jar              Independent component library
build/gui/                             Compiled application classes
GUI-GUIDE.md
```

The component source and its rules are unchanged. Application compilation uses the component JAR as a dependency and does not compile component source into the GUI output.

## 3. Complete source code

- [Main.java](app-src/com/sit300/password/app/Main.java)
- [PasswordCheckerUI.java](app-src/com/sit300/password/ui/PasswordCheckerUI.java)

These files contain the full implementation, with no omitted methods, placeholders, or pseudocode. `Main` checks for a graphical desktop and creates the UI through `SwingUtilities.invokeLater`, on the Event Dispatch Thread. `PasswordCheckerUI` builds the window, handles actions, and renders component results.

## 4. Linking the independent JAR

The application imports `com.sit300.password.component.PasswordAnalyzer` and `PasswordResult`. The compiler finds those classes through `-cp dist/password-analyzer.jar`. The runtime also needs this JAR on its classpath, alongside `build/gui`. Copying only the application classes is insufficient.

In IntelliJ IDEA, Eclipse, or NetBeans:

1. Select JDK 17 or later for the application project.
2. Mark `app-src` as the application's source root.
3. Add `dist/password-analyzer.jar` as a library dependency (IntelliJ module dependencies, Eclipse Java Build Path libraries, or NetBeans project libraries).
4. Run `com.sit300.password.app.Main`.

For a clear independent-component demonstration, use a separate application project containing only `app-src` and a copy of the component JAR. Do not also add the component's `src` folder to that application's source path.

## 5. Compile and run on Windows

For everyday use after building, double-click **Run Password Analyzer.bat** in the project folder. It launches the existing JARs without requiring VS Code or a typed command. You can create a desktop shortcut to this file; keep the actual launcher in the project folder next to `dist`. The launcher opens the previously built version, so source edits still require recompilation and packaging using the commands below.

Open PowerShell in the project root. Ensure the JDK's `bin` directory is on PATH. All commands work offline and require no dependency downloads.

The existing component JAR is in `dist/password-analyzer.jar`. If it needs rebuilding, use:

```powershell
New-Item -ItemType Directory -Force build/component, dist | Out-Null
javac --release 17 -d build/component src/com/sit300/password/component/PasswordAnalyzer.java src/com/sit300/password/component/PasswordResult.java
jar --create --file dist/password-analyzer.jar -C build/component .
```

Compile and launch the graphical application:

```powershell
New-Item -ItemType Directory -Force build/gui | Out-Null
javac --release 17 -cp dist/password-analyzer.jar -d build/gui app-src/com/sit300/password/app/Main.java app-src/com/sit300/password/ui/PasswordCheckerUI.java
java -cp "dist/password-analyzer.jar;build/gui" com.sit300.password.app.Main
```

Run each command only after the preceding command succeeds. On this machine the full JDK is installed at `C:\Program Files\Java\jdk-25`. If `jar` is not found, add that JDK directory for the current PowerShell session (adjust for your installation):

```powershell
$env:Path = 'C:\Program Files\Java\jdk-25\bin;' + $env:Path
```

To package the GUI separately, without embedding or copying the component classes:

```powershell
jar --create --file dist/password-analyzer-gui.jar -C build/gui .
java -cp "dist/password-analyzer.jar;dist/password-analyzer-gui.jar" com.sit300.password.app.Main
```

Keep both JARs together when distributing them and launch using the classpath command above; these commands do not configure double-click launching or `java -jar`. A graphical Java runtime is required. On Linux/macOS replace the classpath separator `;` with `:`.

## 6. Component interaction

`analyzePassword()` obtains a temporary `char[]` using `JPasswordField.getPassword()`. It constructs the `String` required by the existing component API and immediately calls `analyzer.analyze(...)`. In a `finally` block it overwrites the temporary array with null characters.

`displayResult()` reads only public result methods:

| UI element | Component API |
| --- | --- |
| Strength text and category color | `getStrength()` |
| Progress bar value and maximum; score text | `getScore()`, `getMaximumScore()` |
| Minimum length | `isMinimumLengthMet()` |
| Uppercase | `hasUppercase()` |
| Lowercase | `hasLowercase()` |
| Number | `hasDigit()` |
| Special character | `hasSpecialCharacter()` |
| Additional length | `isBonusLengthMet()` |
| Predictability display | `isPredictable()` |
| Suggestions, displayed verbatim | `getFeedback()` |

The labels describe the existing component rules. They do not perform validation. Color selection maps the returned classification to a color, without recalculating classification thresholds. An empty suggestion list leaves the suggestion area blank.

Empty input is analyzed normally, yielding the component's zero-point Weak result and failed checks. The UI adds a short input-status message asking the user to enter a password; it does not generate strength advice. `null` is not produced by the normal password-field input path. Unexpected runtime exceptions show a generic error, reset stale results, and do not expose exception details or passwords.

`resetResults()` returns the output to its unchecked state. A document listener calls it whenever input changes, preventing an old result from appearing to describe a newly edited password. `clearForm()` also empties the field, restores masking, deselects Show password, and returns focus to the field.

## 7. Manual interface scenarios to execute

These are proposed scenarios and expected outcomes, not a record of completed GUI testing. Use synthetic examples only.

| Scenario | Expected outcome |
| --- | --- |
| Launch | Masked input, Show password unchecked, Not analyzed, all checks NOT CHECKED, blank feedback |
| Type, show, then hide | Checkbox changes visibility without changing the input |
| Analyze empty input | Weak, 0/6, all checks FAIL, component suggestions and an input-status message |
| Analyze `aB` | Weak, 2/6 |
| Analyze `aB7` | Moderate, 3/6 |
| Analyze `aB7!` | Moderate, 4/6; minimum length fails |
| Analyze `N7!vR2@k` | Strong, 5/6; bonus length fails; component suggests 12 characters |
| Analyze `N7!vR2@kL9#s` | Strong, 6/6; all checks PASS; no suggestions |
| Analyze `Password123!` | Weak, 2/6, predictable Yes, all six checks PASS; predictability suggestion shown |
| Analyze `aaaaaaaa` or `X9!4321z` | Predictable Yes and Weak result from the component |
| Analyze one space | Special-character check passes; input is not trimmed |
| Edit after analysis | Previous score, checklist statuses, and suggestions reset immediately |
| Analyze the same input repeatedly | Same displayed results and suggestions |
| Clear with password visible | Input emptied, masking restored, checkbox unchecked, outputs reset, input focused |
| Press Enter in the input | Same behavior as clicking Analyze |
| Navigate with Tab and button mnemonics | Controls remain operable without a mouse |
| Resize or use larger display scaling | Content and feedback remain reachable through scroll panes |
| Close and restart | Starts with empty input and no saved analysis |
| Compare a synthetic input with the console component call | GUI values match the returned result fields exactly |

The generic exception handler can be reviewed in source. No intentional failure mechanism or alternate analyzer is included merely to trigger it.

## 8. Assumptions and limitations

- This is an educational rule-based assessment, not a security guarantee or a compromised-password lookup. Scoring and predictability limits remain those of the original component.
- The original model permits a short password to be Moderate and a predictable password to pass every individual check while scoring Weak. The GUI preserves those outcomes.
- Analysis runs synchronously on the Event Dispatch Thread to keep this small application understandable. Extremely large pasted inputs can temporarily make the window unresponsive; no arbitrary password-length restriction has been added.
- The UI keeps the input in its password control until Clear or window close. It stores no password in an application field, file, preference, database, or log, and performs no network operations.
- The component accepts `String`, so the UI must create one immutable copy that cannot be explicitly erased. Clearing the temporary array and the field does not guarantee that all JVM memory copies are erased. No component API change is required for integration.
- Show password intentionally exposes the entered text on screen. Clear always restores the hidden default.
- Swing appearance and progress-bar color rendering can vary by platform. Category text and PASS/FAIL labels carry the meaning independently of color.
- Compilation checks establish source/API compatibility only. They do not establish visual quality, keyboard behavior, or completed GUI testing; the group should execute the scenarios above on its own desktop.
