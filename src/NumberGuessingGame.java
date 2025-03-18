import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Random;

public class NumberGuessingGame extends JFrame {
    private int targetNumber; // The number to guess
    private int attempts; // Number of attempts
    private int bestScore = 0; // Track the best score
    private final int minRange = 1, maxRange = 100, maxAttempts = 15; // Ranges and attempt limits
    private final int totalRounds = 3;
    private int currentRound = 1; // Round tracking
    private final int[] roundScores = new int[totalRounds]; // Stores scores
    private final int[] roundAttempts = new int[totalRounds]; // Stores attempts

    // GUI Components
    private final JLabel bestScoreLabel, guessesLeftLabel, headingLabel, resultLabel, leftImageLabel, rightImageLabel, roundLabel;
    private final JTextField guessField;
    private final JButton guessButton, giveUpButton, nextRoundButton;
    private final JButton darkModeToggleButton;
    private boolean isDarkMode = false;

    // Colors
    private final Color LIGHT_MODE_BACKGROUND = new Color(240, 240, 240); // Light grey
    private final Color DARK_MODE_BACKGROUND = Color.DARK_GRAY; // Dark grey

    public NumberGuessingGame() {
        setTitle("GUESS MY NUMBER");
        setSize(600, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        // Set initial background color (light mode)
        getContentPane().setBackground(LIGHT_MODE_BACKGROUND);

        // Score and Attempts UI
        bestScoreLabel = new JLabel("🏆 Best Score: 0");
        bestScoreLabel.setBounds(50, 25, 150, 20);
        bestScoreLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        add(bestScoreLabel);

        guessesLeftLabel = new JLabel("🎯 Guesses Left: " + maxAttempts);
        guessesLeftLabel.setBounds(400, 25, 150, 20);
        guessesLeftLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        add(guessesLeftLabel);

        // Round Label (Numbers in green)
        roundLabel = new JLabel();
        roundLabel.setBounds(220, 5, 150, 20); // Reset height to original
        roundLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        roundLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the text
        updateRoundLabel(); // Initialize the round label
        add(roundLabel);

        // Title
        headingLabel = new JLabel("Guess my number Game", SwingConstants.CENTER);
        headingLabel.setForeground(Color.RED);
        headingLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        headingLabel.setBounds(150, 50, 300, 30);
        add(headingLabel);

        // Load images
        ImageIcon leftIcon = new ImageIcon(getClass().getResource("/resources/question_mark2.png"));
        ImageIcon rightIcon = new ImageIcon(getClass().getResource("/resources/question_mark4.png"));

        leftImageLabel = new JLabel(new ImageIcon(leftIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
        leftImageLabel.setBounds(50, 100, 100, 100);
        add(leftImageLabel);

        rightImageLabel = new JLabel(new ImageIcon(rightIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
        rightImageLabel.setBounds(450, 100, 100, 100);
        add(rightImageLabel);

        // Input field
        JLabel instructionLabel = new JLabel("Enter a number b/w 1-100", SwingConstants.CENTER);
        instructionLabel.setBounds(150, 100, 300, 20);
        instructionLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(instructionLabel);

        guessField = new JTextField();
        guessField.setBounds(275, 130, 50, 25);
        guessField.setHorizontalAlignment(JTextField.CENTER);
        guessField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(guessField);

        // Result Label
        resultLabel = new JLabel("Try and guess the number", SwingConstants.CENTER);
        resultLabel.setBounds(150, 160, 300, 50);
        resultLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultLabel.setForeground(Color.BLACK); // Default to black for light mode
        add(resultLabel);

        // Buttons
        guessButton = new JButton("Guess");
        guessButton.setBounds(250, 220, 100, 30);
        guessButton.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12)); // Bold and monospaced
        guessButton.setEnabled(false); // Initially disabled
        add(guessButton);

        giveUpButton = new JButton("Give up!");
        giveUpButton.setBounds(150, 260, 100, 30);
        giveUpButton.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12)); // Bold and monospaced
        add(giveUpButton);

        nextRoundButton = new JButton("Next Round");
        nextRoundButton.setBounds(350, 260, 100, 30);
        nextRoundButton.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12)); // Bold and monospaced
        nextRoundButton.setEnabled(false); // Initially hidden
        add(nextRoundButton);

        // Dark Mode Toggle Button
        darkModeToggleButton = new JButton();
        darkModeToggleButton.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/resources/moon.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        darkModeToggleButton.setBounds(520, 280, 50, 30);
        darkModeToggleButton.setBorderPainted(false); // Remove border
        darkModeToggleButton.setContentAreaFilled(false); // Remove background
        darkModeToggleButton.setFocusPainted(false); // Remove focus border
        darkModeToggleButton.addActionListener(e -> toggleDarkMode());
        add(darkModeToggleButton);

        // Button Actions
        guessButton.addActionListener(new GuessButtonListener());
        giveUpButton.addActionListener(e -> endRound(false, true));
        nextRoundButton.addActionListener(e -> startNextRound());

        // Add a DocumentListener to the guessField
        guessField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateGuessButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateGuessButtonState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateGuessButtonState();
            }

            private void updateGuessButtonState() {
                // Enable the guessButton if the guessField is not empty, otherwise disable it
                guessButton.setEnabled(!guessField.getText().trim().isEmpty());
            }
        });

        // Auto-focus on Input Field after each guess
        guessField.addActionListener(e -> guessButton.doClick());

        // Pressing Enter triggers the Guess Button
        guessField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "guess");
        guessField.getActionMap().put("guess", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guessButton.doClick();
            }
        });

        startNewGame();
        setVisible(true);
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        if (isDarkMode) {
            getContentPane().setBackground(DARK_MODE_BACKGROUND);
            resultLabel.setForeground(Color.BLACK); // Keep text black in dark mode
            darkModeToggleButton.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/resources/sun.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        } else {
            getContentPane().setBackground(LIGHT_MODE_BACKGROUND);
            resultLabel.setForeground(Color.BLACK); // Keep text black in light mode
            darkModeToggleButton.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/resources/moon.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        }
    }

    private void startNewGame() {
        currentRound = 1; // Reset to round 1
        bestScore = 0; // Reset best score
        bestScoreLabel.setText("🏆 Best Score: 0"); // Reset UI label

        // Reset scores and attempts for all rounds
        for (int i = 0; i < totalRounds; i++) {
            roundScores[i] = 0;
            roundAttempts[i] = 0;
        }

        // Reset the nextRoundButton action listener
        for (ActionListener al : nextRoundButton.getActionListeners()) {
            nextRoundButton.removeActionListener(al);
        }
        nextRoundButton.addActionListener(e -> startNextRound());

        // Reset UI components
        nextRoundButton.setEnabled(false); // Disable "Check Results" on fresh start
        nextRoundButton.setText("Next Round"); // Reset button text
        guessesLeftLabel.setText("🎯 Guesses Left: " + maxAttempts);
        resultLabel.setText("Round " + currentRound + " - Try and guess my number!");
        guessField.setEnabled(true);
        guessButton.setEnabled(false);
        giveUpButton.setEnabled(true);
        guessField.setText("");
        guessField.requestFocusInWindow(); // Auto-focus on input field

        // Reset target number and attempts
        targetNumber = new Random().nextInt(maxRange - minRange + 1) + minRange;
        attempts = 0;

        updateRoundLabel(); // Update the round label
    }

    private void startNextRound() {
        if (currentRound > totalRounds) {
            showGameOver(); // Show Game Over dialog only after all rounds are completed
            return;
        }

        targetNumber = new Random().nextInt(maxRange - minRange + 1) + minRange;
        attempts = 0;
        guessesLeftLabel.setText("🎯 Guesses Left: " + maxAttempts);
        resultLabel.setText("Round " + currentRound + " - Try and guess my number!");
        guessField.setEnabled(true);
        guessButton.setEnabled(false);
        giveUpButton.setEnabled(true);
        nextRoundButton.setEnabled(false);
        guessField.setText("");
        guessField.requestFocusInWindow(); // Auto-focus on input field

        updateRoundLabel(); // Update the round label
    }

    private void updateRoundLabel() {
        // Set the round label text with numbers in green
        roundLabel.setText("<html>Round: <font color='green'>" + currentRound + "</font>/<font color='green'>" + totalRounds + "</font></html>");
    }

    private class GuessButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int guess = Integer.parseInt(guessField.getText());
                if (guess < minRange || guess > maxRange) {
                    resultLabel.setText("<html><div style='text-align: center;'>Please enter a number between " + minRange + " and " + maxRange + ".</div></html>");
                    guessField.setText(""); // Clear the input field
                    return;
                }

                attempts++;
                guessesLeftLabel.setText("🎯 Guesses Left: " + (maxAttempts - attempts));

                if (guess == targetNumber) {
                    int score = 100 / attempts;
                    roundScores[currentRound - 1] = score;
                    roundAttempts[currentRound - 1] = attempts;

                    // Update the best score if the current score is higher
                    if (score > bestScore) {
                        bestScore = score;
                        bestScoreLabel.setText("🏆 Best Score: " + bestScore);
                    }

                    // Display congratulations message
                    resultLabel.setText("<html><div style='text-align: center;'>Congratulations! The number was " + targetNumber + ".<br>You scored " + score + " points!</div></html>");
                    endRound(true, false);
                } else if (guess < targetNumber) {
                    resultLabel.setText("<html><div style='text-align: center;'>Too low! Try again.</div></html>");
                } else {
                    resultLabel.setText("<html><div style='text-align: center;'>Too high! Try again.</div></html>");
                }

                // Reset the input field after every guess
                guessField.setText("");
                guessField.requestFocusInWindow(); // Auto-focus on input field

                // Check if the user has run out of attempts
                if (attempts >= maxAttempts) {
                    resultLabel.setText("<html><div style='text-align: center;'>Out of attempts! The number was " + targetNumber + ".</div></html>");
                    endRound(false, false);
                }
            } catch (NumberFormatException ex) {
                resultLabel.setText("<html><div style='text-align: center;'>Invalid input! Enter a number.</div></html>"); // Handle invalid input
                guessField.setText(""); // Clear the input field on invalid input
            }
        }
    }

    private void endRound(boolean won, boolean gaveUp) {
        guessField.setEnabled(false);
        guessButton.setEnabled(false);
        giveUpButton.setEnabled(false);

        // Store the correct number of attempts used
        roundAttempts[currentRound - 1] = maxAttempts - Integer.parseInt(guessesLeftLabel.getText().replaceAll("[^0-9]", ""));

        if (currentRound == totalRounds) { // Stop at last round
            nextRoundButton.setText("Results");
            // Remove all existing action listeners
            for (ActionListener al : nextRoundButton.getActionListeners()) {
                nextRoundButton.removeActionListener(al);
            }
            // Add a new action listener for "Check Results"
            nextRoundButton.addActionListener(e -> showGameOver());
        }
        nextRoundButton.setEnabled(true);

        if (!won) {
            resultLabel.setText("Round " + currentRound + " Over! Number was " + targetNumber);
        }

        // Increment the round counter only after the round is completed
        if (currentRound < totalRounds) {
            currentRound++;
        }
    }

    private void showGameOver() {
        // Create a custom dialog for Game Over
        JDialog gameOverDialog = new JDialog(this, "", true);
        gameOverDialog.setSize(400, 300); // Adjust size
        gameOverDialog.setLayout(new BorderLayout());
        gameOverDialog.setLocationRelativeTo(this); // Center the dialog
        gameOverDialog.setUndecorated(true); // Remove title bar and buttons

        // Set background color based on dark mode
        if (isDarkMode) {
            gameOverDialog.getContentPane().setBackground(DARK_MODE_BACKGROUND);
        } else {
            gameOverDialog.getContentPane().setBackground(LIGHT_MODE_BACKGROUND);
        }

        // === "Game Over" Title at the TOP ===
        JLabel gameOverLabel = new JLabel("Game Over! 🎮 ", SwingConstants.CENTER);
        gameOverLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        gameOverLabel.setForeground(Color.RED);
        gameOverLabel.setBorder(BorderFactory.createEmptyBorder(35, 5, 10, 0)); // Space below the title
        gameOverDialog.add(gameOverLabel, BorderLayout.NORTH); // Place at the top

        // === Center Panel: Image + Results Side by Side ===
        JPanel centerPanel = new JPanel(new GridBagLayout()); // Use GridBagLayout for centering
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add padding

        // Load and add the image
        ImageIcon questionMarkIcon = new ImageIcon(getClass().getResource("/resources/question_mark.png"));
        JLabel imageLabel = new JLabel(new ImageIcon(questionMarkIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH)));

        // Panel for results
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));

        for (int i = 0; i < totalRounds; i++) {
            JLabel roundResultLabel = new JLabel("<html><b>Round " + (i + 1) + ":</b> Score = " + roundScores[i] + ", Attempts = " + roundAttempts[i] + "</html>");
            roundResultLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            roundResultLabel.setForeground(Color.BLACK); // Always black for readability
            resultsPanel.add(roundResultLabel);
        }

        // Add image on the left
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(imageLabel, gbc);

        // Add results on the right
        gbc.gridx = 1;
        gbc.gridy = 0;
        centerPanel.add(resultsPanel, gbc);

        gameOverDialog.add(centerPanel, BorderLayout.CENTER); // Add main content to the center

        // === Buttons at the bottom ===
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        JButton restartButton = new JButton("Restart Game");
        restartButton.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12)); // Bold and monospaced
        restartButton.addActionListener(e -> {
            gameOverDialog.dispose();
            startNewGame(); // Restart the game
        });
        buttonPanel.add(restartButton);

        JButton closeButton = new JButton("Close Game");
        closeButton.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12)); // Bold and monospaced
        closeButton.addActionListener(e -> {
            gameOverDialog.dispose();
            dispose(); // Close the main game window
        });
        buttonPanel.add(closeButton);

        gameOverDialog.add(buttonPanel, BorderLayout.SOUTH); // Buttons at bottom
        gameOverDialog.setVisible(true);
    }

    public static void main(String[] args) {
        new NumberGuessingGame();
    }
}