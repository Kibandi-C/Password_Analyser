package com.sit300.password.ui;

import com.sit300.password.component.PasswordAnalyzer;
import com.sit300.password.component.PasswordResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Presentation and user actions only; all password analysis belongs to the component. */
public final class PasswordCheckerUI {
    private final PasswordAnalyzer analyzer = new PasswordAnalyzer();
    private final JFrame frame = new JFrame("Password Strength Analyzer");
    private final JPasswordField passwordField = new JPasswordField(28);
    private final JCheckBox showPassword = new JCheckBox("Show password");
    private final char hiddenEchoCharacter;
    private final JLabel strengthLabel = new JLabel();
    private final JProgressBar strengthBar = new JProgressBar(0, PasswordAnalyzer.MAX_SCORE);
    private final JLabel statusLabel = new JLabel();
    private final JLabel predictabilityLabel = new JLabel();
    private final JTextArea feedbackArea = new JTextArea(5, 38);

    private final String[] requirementNames = {
        "Minimum length (8 characters)",
        "Uppercase English letter (A-Z)",
        "Lowercase English letter (a-z)",
        "Number (0-9)",
        "Special character (outside ASCII letters and digits)",
        "Additional length (12 characters)"
    };
    private final JLabel[] requirementLabels = new JLabel[requirementNames.length];

    /** Construct and use this interface on the Event Dispatch Thread. */
    public PasswordCheckerUI() {
        char defaultEcho = passwordField.getEchoChar();
        hiddenEchoCharacter = defaultEcho == 0 ? '*' : defaultEcho;
        passwordField.setEchoChar(hiddenEchoCharacter);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel heading = new JPanel(new GridLayout(0, 1, 0, 6));
        JLabel title = new JLabel("Password Strength Analyzer");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        heading.add(title);
        heading.add(new JLabel("Check a password using the reusable analysis component."));
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        content.add(heading);
        content.add(createInputPanel());
        content.add(createResultPanel());
        content.add(createChecklistPanel());
        content.add(createFeedbackPanel());

        JLabel educationalNotice = new JLabel("Educational rule-based assessment; no security guarantee.");
        educationalNotice.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JPanel footer = new JPanel(new BorderLayout());
        footer.add(educationalNotice, BorderLayout.CENTER);
        content.add(footer);

        frame.setContentPane(new JScrollPane(content));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                passwordField.setText("");
            }
        });
        frame.setSize(660, 720);
        frame.setMinimumSize(new Dimension(560, 480));
        frame.setLocationRelativeTo(null);

        // Editing invalidates the previous display; it does not run analysis.
        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) { resetResults(); }

            @Override
            public void removeUpdate(DocumentEvent event) { resetResults(); }

            @Override
            public void changedUpdate(DocumentEvent event) { resetResults(); }
        });
        resetResults();
    }

    public void show() {
        frame.setVisible(true);
        passwordField.requestFocusInWindow();
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Password input"));
        JLabel inputLabel = new JLabel("Password:");
        inputLabel.setLabelFor(passwordField);
        panel.add(inputLabel, BorderLayout.WEST);
        panel.add(passwordField, BorderLayout.CENTER);

        JButton analyzeButton = new JButton("Analyze");
        JButton clearButton = new JButton("Clear");
        analyzeButton.setMnemonic('A');
        clearButton.setMnemonic('C');
        showPassword.setMnemonic('S');
        analyzeButton.addActionListener(event -> analyzePassword());
        clearButton.addActionListener(event -> clearForm());
        showPassword.addActionListener(event -> passwordField.setEchoChar(
                showPassword.isSelected() ? (char) 0 : hiddenEchoCharacter));
        frame.getRootPane().setDefaultButton(analyzeButton);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(showPassword);
        controls.add(analyzeButton);
        controls.add(clearButton);
        panel.add(controls, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Strength result"));
        strengthLabel.setFont(strengthLabel.getFont().deriveFont(Font.BOLD, 20f));
        strengthBar.setStringPainted(true);
        strengthBar.getAccessibleContext().setAccessibleName("Password strength score");
        panel.add(strengthLabel);
        panel.add(strengthBar);
        panel.add(predictabilityLabel);
        panel.add(statusLabel);
        return panel;
    }

    private JPanel createChecklistPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Validation checklist"));
        for (int i = 0; i < requirementLabels.length; i++) {
            requirementLabels[i] = new JLabel();
            panel.add(requirementLabels[i]);
        }
        return panel;
    }

    private JPanel createFeedbackPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Suggestions from the component"));
        feedbackArea.setEditable(false);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setFont(new JLabel().getFont());
        feedbackArea.getAccessibleContext().setAccessibleName("Improvement suggestions");
        panel.add(new JScrollPane(feedbackArea), BorderLayout.CENTER);
        return panel;
    }

    private void analyzePassword() {
        char[] characters = passwordField.getPassword();
        try {
            // The component requires String. Do not keep that String in a field.
            PasswordResult result = analyzer.analyze(new String(characters));
            displayResult(result);
            statusLabel.setText(characters.length == 0
                    ? "Enter a password; the empty-input result is shown above."
                    : "Analysis complete. Editing the password resets these results.");
        } catch (RuntimeException exception) {
            resetResults();
            statusLabel.setText("Analysis could not be completed. Clear the field and try again.");
            // Do not display exception details or log submitted data.
            JOptionPane.showMessageDialog(frame,
                    "Unable to analyze the input. Please clear the field and try again.",
                    "Analysis error", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(characters, '\0');
        }
    }

    private void displayResult(PasswordResult result) {
        strengthLabel.setText(result.getStrength());
        // Colors present the returned category; no score thresholds are calculated here.
        Color categoryColor = switch (result.getStrength()) {
            case "Weak" -> new Color(160, 35, 35);
            case "Moderate" -> new Color(130, 85, 0);
            case "Strong" -> new Color(25, 110, 55);
            default -> Color.DARK_GRAY;
        };
        strengthLabel.setForeground(categoryColor);
        strengthBar.setForeground(categoryColor);
        strengthBar.setMaximum(result.getMaximumScore());
        strengthBar.setValue(result.getScore());
        strengthBar.setString("Score: " + result.getScore() + " / " + result.getMaximumScore());

        boolean[] passed = {
            result.isMinimumLengthMet(), result.hasUppercase(), result.hasLowercase(),
            result.hasDigit(), result.hasSpecialCharacter(), result.isBonusLengthMet()
        };
        for (int i = 0; i < requirementLabels.length; i++) {
            requirementLabels[i].setText((passed[i] ? "PASS - " : "FAIL - ") + requirementNames[i]);
        }
        predictabilityLabel.setText("Predictable pattern detected: "
                + (result.isPredictable() ? "Yes" : "No"));

        // Display the component's suggestions verbatim, without adding advice.
        feedbackArea.setText(String.join(System.lineSeparator(), result.getFeedback()));
        feedbackArea.setCaretPosition(0);
    }

    private void resetResults() {
        strengthLabel.setText("Not analyzed");
        strengthLabel.setForeground(Color.DARK_GRAY);
        strengthBar.setForeground(Color.GRAY);
        strengthBar.setMaximum(PasswordAnalyzer.MAX_SCORE);
        strengthBar.setValue(0);
        strengthBar.setString("Not analyzed");
        predictabilityLabel.setText("Predictable pattern detected: Not checked");
        statusLabel.setText("Enter a password and select Analyze.");
        for (int i = 0; i < requirementLabels.length; i++) {
            requirementLabels[i].setText("NOT CHECKED - " + requirementNames[i]);
        }
        feedbackArea.setText("");
    }

    private void clearForm() {
        passwordField.setText("");
        showPassword.setSelected(false);
        passwordField.setEchoChar(hiddenEchoCharacter);
        resetResults();
        passwordField.requestFocusInWindow();
    }
}
