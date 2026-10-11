package com.sit300.password.demo;

import com.sit300.password.component.PasswordAnalyzer;
import com.sit300.password.component.PasswordResult;

/** External consumer of the component; all terminal output stays here. */
public final class ConsoleDemo {
    public static void main(String[] args) {
        PasswordAnalyzer analyzer = new PasswordAnalyzer();
        // Synthetic example only. The submitted value is not printed.
        PasswordResult result = analyzer.analyze("N7!vR2@k");

        System.out.println("Strength: " + result.getStrength());
        System.out.println("Score: " + result.getScore() + "/" + result.getMaximumScore());
        System.out.println("Minimum length: " + result.isMinimumLengthMet());
        System.out.println("Uppercase: " + result.hasUppercase());
        System.out.println("Lowercase: " + result.hasLowercase());
        System.out.println("Digit: " + result.hasDigit());
        System.out.println("Special character: " + result.hasSpecialCharacter());
        System.out.println("Bonus length: " + result.isBonusLengthMet());
        System.out.println("Predictable: " + result.isPredictable());
        for (String suggestion : result.getFeedback()) {
            System.out.println("- " + suggestion);
        }
    }
}
