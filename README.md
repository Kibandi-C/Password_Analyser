# PasswordAnalyzer usage guide

## 1. Responsibility

`PasswordAnalyzer` evaluates a supplied string using six educational rules and returns an analysis result. It has no input handling, terminal output, GUI, database, network access, or external dependencies. The component does not retain or log passwords or put them in results. Java strings cannot be explicitly erased; this API does not promise secure memory erasure.

The score is a simplified, educational, rule-based assessment, **not a guarantee of password security**. Predictability checks cannot detect all weak or compromised passwords.

## 2. Architecture and public API

Both component classes belong to `com.sit300.password.component`.

- `PasswordAnalyzer`: public no-argument constructor; primary method `public PasswordResult analyze(String password)`; public constant `MAX_SCORE = 6`. It is stateless and reusable.
- `PasswordResult`: immutable outcome created by the analyzer. Its constructor is package-private, so consuming applications obtain results through `analyze`.

| Result method | Meaning |
| --- | --- |
| `getStrength()` | `Weak`, `Moderate`, or `Strong` |
| `getScore()` | Final score after any predictability cap |
| `getMaximumScore()` | Always 6 |
| `isMinimumLengthMet()` | At least 8 characters |
| `hasUppercase()` | Contains A-Z |
| `hasLowercase()` | Contains a-z |
| `hasDigit()` | Contains 0-9 |
| `hasSpecialCharacter()` | Contains a character outside ASCII letters/digits |
| `isBonusLengthMet()` | At least 12 characters |
| `isPredictable()` | Matches a documented heuristic |
| `getFeedback()` | Unmodifiable list of suggestions |

Empty strings are valid: score 0, Weak, all six validation flags false, predictability false. `null` throws `NullPointerException` with a descriptive message. Input is not trimmed. Length counts Unicode code points (an emoji counts as one, though a combined visible symbol can contain multiple code points). Spaces, punctuation, and non-ASCII characters qualify as special characters, exactly as required. Letter and digit rules remain ASCII-only.

Each of the six flags earns one point. Scores 0-2 are Weak, 3-4 Moderate, and 5-6 Strong. Predictability caps the score at 2 without changing the actual validation flags. The specified model allows a password below eight characters to be Moderate; minimum length is a scoring rule, not a separate acceptance gate.

## 3. Folder and package structure

```text
src/com/sit300/password/component/
    PasswordAnalyzer.java
    PasswordResult.java
examples/com/sit300/password/demo/
    ConsoleDemo.java
tests/com/sit300/password/tests/
    PasswordAnalyzerTest.java
README.md
```

Generated `build/` holds class files; `dist/` holds the reusable JAR. Neither examples nor tests are packaged in the component JAR.

## 4. Complete PasswordAnalyzer code

See [PasswordAnalyzer.java](src/com/sit300/password/component/PasswordAnalyzer.java). This is the complete implementation, with all validation, scoring, and feedback helpers private.

## 5. Complete PasswordResult code

See [PasswordResult.java](src/com/sit300/password/component/PasswordResult.java). Private final fields and a defensive, unmodifiable copy of feedback protect results from consumer modification.

## 6. External console demonstration

See [ConsoleDemo.java](examples/com/sit300/password/demo/ConsoleDemo.java). Its essential integration is:

```java
import com.sit300.password.component.PasswordAnalyzer;
import com.sit300.password.component.PasswordResult;

PasswordAnalyzer analyzer = new PasswordAnalyzer();
PasswordResult result = analyzer.analyze("N7!vR2@k");
System.out.println(result.getStrength());
```

The complete demo prints Strong, score 5/6, the individual flags, and a suggestion to reach 12 characters. It does not print the sample password. A future Swing application or another Java application can call exactly the same API and display the returned values using its own interface.

## 7. Compile, run, and package with the JDK

Use JDK 17 or later. Run these PowerShell commands from the project root:

Ensure the full JDK `bin` directory is on PATH, including `jar`. On this machine the JDK is at `C:\Program Files\Java\jdk-25`; if `jar` is not recognized, run `$env:Path = 'C:\Program Files\Java\jdk-25\bin;' + $env:Path` first. Adjust that path for other machines.

```powershell
New-Item -ItemType Directory -Force build/component, build/demo, build/tests, dist | Out-Null
javac --release 17 -d build/component src/com/sit300/password/component/PasswordAnalyzer.java src/com/sit300/password/component/PasswordResult.java
jar --create --file dist/password-analyzer.jar -C build/component .
javac --release 17 -cp dist/password-analyzer.jar -d build/demo examples/com/sit300/password/demo/ConsoleDemo.java
java -cp "dist/password-analyzer.jar;build/demo" com.sit300.password.demo.ConsoleDemo
```

Copy `dist/password-analyzer.jar` into another project's library folder and add it to that project's compile and runtime classpaths. Import the two classes using their package names. This is a library JAR, so it does not need a main class and is not launched with `java -jar`. On Linux/macOS, use `:` instead of `;` between classpath entries and create folders with `mkdir -p`.

Optional dependency-free regression checks against the packaged JAR:

```powershell
javac --release 17 -cp dist/password-analyzer.jar -d build/tests tests/com/sit300/password/tests/PasswordAnalyzerTest.java
java -cp "dist/password-analyzer.jar;build/tests" com.sit300.password.tests.PasswordAnalyzerTest
jar --list --file dist/password-analyzer.jar
```

## 8. Important methods

- `analyze`: rejects null, computes all rule flags, obtains the final score and classification, creates feedback, and returns a result. Repeated calls with the same input produce the same values.
- `meetsMinimumLength`: checks whether the calculated character count is at least eight.
- `hasUppercase`, `hasLowercase`, `hasDigit`: loop over characters and compare with the explicit ASCII ranges. Each stops as soon as a match is found.
- `hasSpecialCharacter`: returns true when a character is neither an ASCII letter nor an ASCII digit.
- `isPredictable`: uses `Locale.ROOT` for consistent case-insensitive matching. It checks for the substrings `password`, `qwerty`, `letmein`, `welcome`, and `admin`; any four-character consecutive window of the forward/reverse digit, English alphabet, or QWERTY keyboard-row sequences listed in the source; or an entire password consisting of at least three copies of one character, ignoring case. For example, `abcd`, `4321`, and `asdf` match. It does not recognize every dictionary word, substitution such as `p@ssw0rd`, repeated multi-character block, or breached password. Substring matching may also flag otherwise unusual passwords.
- `calculateScore`: adds one point per satisfied rule, including the 12-character bonus, then applies `Math.min(score, 2)` for predictable inputs.
- `classifyStrength`: applies the three specified score bands.
- `generateFeedback`: adds suggestions only for missing rules or detected predictability. Below eight characters it suggests the minimum; from eight through eleven it suggests the optional 12-character bonus. A non-predictable six-point result has an empty suggestion list, which is not a security guarantee.
- `PasswordResult` constructor and getters: capture the outcome and expose it without setters. `List.copyOf` prevents callers from editing the stored feedback.

## 9. Suggested manual tests

Replace the synthetic demo input, recompile the demo, and run it. Never use real passwords for demonstrations.

| Input | Score | Strength | Predictable | Main observation |
| --- | --- | --- | --- | --- |
| Empty string | 0 | Weak | No | All validation flags false; five suggestions |
| `a` | 1 | Weak | No | Lowercase only |
| `aB` | 2 | Weak | No | Both letter cases |
| `aB7` | 3 | Moderate | No | Digit adds a point |
| `aB7!` | 4 | Moderate | No | Special character adds a point |
| `N7!vR2@` | 4 | Moderate | No | Seven characters; minimum fails |
| `N7!vR2@k` | 5 | Strong | No | Eight characters; suggest 12 |
| `N7!vR2@kL9#` | 5 | Strong | No | Eleven characters; no bonus |
| `N7!vR2@kL9#s` | 6 | Strong | No | Twelve characters; no suggestions |
| `Password123!` | 2 | Weak | Yes | All six flags true; common word triggers cap |
| `aaaaaaaa` | 2 | Weak | Yes | Repeated-character rule |
| `X9!abcdZ` | 2 | Weak | Yes | Alphabet sequence |
| `X9!4321z` | 2 | Weak | Yes | Reverse numeric sequence |
| `X9!asdfZ` | 2 | Weak | Yes | Keyboard sequence |
| One space | 1 | Weak | No | Special character; not trimmed |
| `Éé١` | 1 | Weak | No | Special only; non-ASCII letters/digit do not satisfy ASCII rules |
| Seven identical emoji | 1 | Weak | Yes | Seven code points, below minimum |
| Eight identical emoji | 2 | Weak | Yes | Minimum length satisfied |
| `null` (Java value) | N/A | N/A | N/A | Descriptive NullPointerException |

Also verify that repeating an analysis returns identical field values and feedback, and that modifying `getFeedback()` throws `UnsupportedOperationException`. Check that the packaged JAR contains only the component classes and standard manifest metadata.

Local verification: compiled with JDK 25 using `--release 17`; the standalone regression checks and external console demo passed against the packaged JAR. JAR contents were checked: only the two component classes plus package directories and manifest metadata. Execution on a Java 17 runtime was not separately tested.
