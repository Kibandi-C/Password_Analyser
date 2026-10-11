package com.sit300.password.tests;

import com.sit300.password.component.PasswordAnalyzer;
import com.sit300.password.component.PasswordResult;

/** Small standalone regression check, with no testing dependencies. */
public final class PasswordAnalyzerTest {
    public static void main(String[] args) {
        PasswordAnalyzer analyzer = new PasswordAnalyzer();
        String[] inputs = {"", "a", "aB", "aB7", "aB7!", "N7!vR2@k",
                "N7!vR2@kL9#", "N7!vR2@kL9#s", "Password123!",
                "aaaaaaaa", "X9!abcdZ", "X9!4321z", "X9!asdfZ",
                "pAsSwOrD", "aa", "aaa", "!!!!!!!!!!!!"};
        int[] scores = {0, 1, 2, 3, 4, 5, 5, 6, 2, 2, 2, 2, 2, 2, 1, 1, 2};
        boolean[] predictable = {false, false, false, false, false, false,
                false, false, true, true, true, true, true, true, false, true, true};
        for (int i = 0; i < inputs.length; i++) {
            PasswordResult result = analyzer.analyze(inputs[i]);
            check(result.getScore() == scores[i], "score case " + i);
            check(result.isPredictable() == predictable[i], "predictability case " + i);
            String expected = scores[i] <= 2 ? "Weak" : scores[i] <= 4 ? "Moderate" : "Strong";
            check(result.getStrength().equals(expected), "classification case " + i);
            check(result.getMaximumScore() == 6, "maximum score");
            check(result.getFeedback().equals(analyzer.analyze(inputs[i]).getFeedback()),
                    "deterministic feedback");
        }
        PasswordResult empty = analyzer.analyze("");
        check(!empty.isMinimumLengthMet() && !empty.hasUppercase()
                && !empty.hasLowercase() && !empty.hasDigit()
                && !empty.hasSpecialCharacter() && !empty.isBonusLengthMet(), "empty flags");
        check(empty.getFeedback().size() == 5, "empty feedback");
        PasswordResult full = analyzer.analyze("N7!vR2@kL9#s");
        check(full.isMinimumLengthMet() && full.hasUppercase() && full.hasLowercase()
                && full.hasDigit() && full.hasSpecialCharacter() && full.isBonusLengthMet(),
                "all flags");
        check(full.getFeedback().isEmpty(), "no missing rules");
        check(analyzer.analyze("Password123!").getFeedback().size() == 1, "cap feedback");
        PasswordResult unicode = analyzer.analyze("\u00c9\u00e9\u0661");
        check(!unicode.hasUppercase() && !unicode.hasLowercase() && !unicode.hasDigit()
                && unicode.hasSpecialCharacter(), "ASCII-only categories");
        check(analyzer.analyze(" ").hasSpecialCharacter(), "space is special");
        check(!analyzer.analyze("\ud83d\ude00".repeat(7)).isMinimumLengthMet(), "code point length");
        check(analyzer.analyze("\ud83d\ude00".repeat(8)).isMinimumLengthMet(), "eight code points");
        try {
            full.getFeedback().add("changed");
            throw new AssertionError("feedback must be immutable");
        } catch (UnsupportedOperationException expected) {
            // Expected: callers cannot modify the result.
        }
        try {
            analyzer.analyze(null);
            throw new AssertionError("null must be rejected");
        } catch (NullPointerException expected) {
            // Null is an invalid API argument; empty strings are valid inputs.
        }
        System.out.println("All password analyzer checks passed.");
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
    }
}
