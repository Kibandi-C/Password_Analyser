package com.sit300.password.component;

import java.util.List;

/** Immutable analysis outcome. Contains no submitted password. */
public final class PasswordResult {
    private final String strength;
    private final int score;
    private final int maximumScore;
    private final boolean minimumLengthMet;
    private final boolean uppercasePresent;
    private final boolean lowercasePresent;
    private final boolean digitPresent;
    private final boolean specialCharacterPresent;
    private final boolean bonusLengthMet;
    private final boolean predictable;
    private final List<String> feedback;

    // Package-private: consumers obtain consistent results through the analyzer.
    PasswordResult(String strength, int score, int maximumScore,
            boolean minimumLengthMet, boolean uppercasePresent,
            boolean lowercasePresent, boolean digitPresent,
            boolean specialCharacterPresent, boolean bonusLengthMet,
            boolean predictable, List<String> feedback) {
        this.strength = strength;
        this.score = score;
        this.maximumScore = maximumScore;
        this.minimumLengthMet = minimumLengthMet;
        this.uppercasePresent = uppercasePresent;
        this.lowercasePresent = lowercasePresent;
        this.digitPresent = digitPresent;
        this.specialCharacterPresent = specialCharacterPresent;
        this.bonusLengthMet = bonusLengthMet;
        this.predictable = predictable;
        this.feedback = List.copyOf(feedback);
    }

    public String getStrength() { return strength; }
    public int getScore() { return score; }
    public int getMaximumScore() { return maximumScore; }
    public boolean isMinimumLengthMet() { return minimumLengthMet; }
    public boolean hasUppercase() { return uppercasePresent; }
    public boolean hasLowercase() { return lowercasePresent; }
    public boolean hasDigit() { return digitPresent; }
    public boolean hasSpecialCharacter() { return specialCharacterPresent; }
    public boolean isBonusLengthMet() { return bonusLengthMet; }
    public boolean isPredictable() { return predictable; }

    /** Returns an unmodifiable list of improvement suggestions. */
    public List<String> getFeedback() { return feedback; }
}
