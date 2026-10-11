package com.sit300.password.app;

import com.sit300.password.ui.PasswordCheckerUI;
import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

/** Starts the desktop application on Swing's Event Dispatch Thread. */
public final class Main {
    private Main() {
        // This class only provides the application entry point.
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("A graphical desktop is required to run this application.");
            return;
        }
        SwingUtilities.invokeLater(() -> new PasswordCheckerUI().show());
    }
}
