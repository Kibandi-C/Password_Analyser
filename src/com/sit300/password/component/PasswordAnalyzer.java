package com.sit300.password.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Reusable, stateless educational password analyzer.
 * Scores are simplified rule checks, not a guarantee of password security.
 * Submitted passwords are never retained, logged, or included in results.
 */
public final class PasswordAnalyzer {
    public static final int MAX_SCORE = 6;

    private static final List<String> COMMON_WORDS = List.of(
            "password", "qwerty", "letmein", "welcome", "admin");
    private static final List<String> SEQUENCES = List.of(
            "0123456789", "9876543210",
            "abcdefghijklmnopqrstuvwxyz", "zyxwvutsrqponmlkjihgfedcba",
            "qwertyuiop", "poiuytrewq", "asdfghjkl", "lkjhgfdsa",
            "zxcvbnm", "mnbvcxz");

    /**
     * Analyzes a non-null password without modifying or retaining it.
     * Length counts Unicode code points; letter/digit checks are ASCII only.
     * Whitespace and non-ASCII characters qualify as special characters.
     *
     * @throws NullPointerException if password is null
     */
    public PasswordResult analyze(String password) {
        Objects.requireNonNull(password, "password must not be null");

        int length = password.codePointCount(0, password.length());
        boolean minimumLength = meetsMinimumLength(length);
        boolean uppercase = hasUppercase(password);
        boolean lowercase = hasLowercase(password);
        boolean digit = hasDigit(password);
        boolean special = hasSpecialCharacter(password);
        boolean bonusLength = length >= 12;
        boolean predictable = isPredictable(password);

        int score = calculateScore(minimumLength, uppercase, lowercase,
                digit, special, bonusLength, predictable);
        String strength = classifyStrength(score);
        List<String> feedback = generateFeedback(minimumLength, uppercase,
                lowercase, digit, special, bonusLength, predictable);

        return new PasswordResult(strength, score, MAX_SCORE, minimumLength,
                uppercase, lowercase, digit, special, bonusLength,
                predictable, feedback);
    }

    private boolean meetsMinimumLength(int length) {
        return length >= 8;
    }

    private boolean hasUppercase(String password) {
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                return true;
            }
        }
        return false;
    }

    private boolean hasLowercase(String password) {
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (c >= 'a' && c <= 'z') {
                return true;
            }
        }
        return false;
    }

    private boolean hasDigit(String password) {
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (c >= '0' && c <= '9') {
                return true;
            }
        }
        return false;
    }

    private boolean hasSpecialCharacter(String password) {
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            boolean asciiLetter = (c >= 'A' && c <= 'Z')
                    || (c >= 'a' && c <= 'z');
            boolean asciiDigit = c >= '0' && c <= '9';
            if (!asciiLetter && !asciiDigit) {
                return true;
            }
        }
        return false;
    }

    /** Simple heuristics only: this does not check breached-password databases. */
    private boolean isPredictable(String password) {
        if (password.isEmpty()) {
            return false;
        }
        String lower = password.toLowerCase(Locale.ROOT);
        for (String word : COMMON_WORDS) {
            if (lower.contains(word)) {
                return true;
            }
        }

        // Match any consecutive four-character window in the listed sequences.
        for (String sequence : SEQUENCES) {
            for (int i = 0; i <= sequence.length() - 4; i++) {
                if (lower.contains(sequence.substring(i, i + 4))) {
                    return true;
                }
            }
        }

        // Three or more copies of the same code point, ignoring letter case.
        int first = lower.codePointAt(0);
        int count = 0;
        for (int i = 0; i < lower.length();) {
            int current = lower.codePointAt(i);
            if (current != first) {
                return false;
            }
            count++;
            i += Character.charCount(current);
        }
        return count >= 3;
    }

    private int calculateScore(boolean minimumLength, boolean uppercase,
            boolean lowercase, boolean digit, boolean special,
            boolean bonusLength, boolean predictable) {
        int score = 0;
        if (minimumLength) { score++; }
        if (uppercase) { score++; }
        if (lowercase) { score++; }
        if (digit) { score++; }
        if (special) { score++; }
        if (bonusLength) { score++; }
        return predictable ? Math.min(score, 2) : score;
    }

    private String classifyStrength(int score) {
        if (score <= 2) {
            return "Weak";
        }
        if (score <= 4) {
            return "Moderate";
        }
        return "Strong";
    }

    private List<String> generateFeedback(boolean minimumLength,
            boolean uppercase, boolean lowercase, boolean digit,
            boolean special, boolean bonusLength, boolean predictable) {
        List<String> feedback = new ArrayList<>();
        if (!minimumLength) {
            feedback.add("Increase the password length to at least 8 characters.");
        } else if (!bonusLength) {
            feedback.add("Use at least 12 characters for the additional length point.");
        }
        if (!uppercase) { feedback.add("Include uppercase English letters (A-Z)."); }
        if (!lowercase) { feedback.add("Include lowercase English letters (a-z)."); }
        if (!digit) { feedback.add("Add numbers (0-9)."); }
        if (!special) { feedback.add("Add special characters (outside ASCII letters and digits)."); }
        if (predictable) {
            feedback.add("Avoid common password words, obvious sequences, or repeated characters.");
        }
        return feedback;
    }
}
