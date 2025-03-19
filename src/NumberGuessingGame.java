import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class NumberGuessingGame extends JFrame {
    // Game state variables
    private int targetNumber; // The number the player needs to guess
    private int attempts; // Number of attempts made by the player
    private int bestScore = 0; // Best score across all rounds
    private int currentRound = 1; // Current round number
    private final int minRange = 1, maxRange = 100, maxAttempts = 15, totalRounds = 3; // Game settings
    private final int[] roundScores = new int[totalRounds]; // Stores scores for each round
    private final int[] roundAttempts = new int[totalRounds]; // Stores attempts for each round

    // GUI components
    private final JLabel bestScoreLabel, guessesLeftLabel, headingLabel, resultLabel, leftImageLabel, rightImageLabel, roundLabel;
    private final JTextField guessField; // Input field for the player's guess
    private final JButton guessButton, giveUpButton, nextRoundButton, darkModeToggleButton; // Buttons for actions
    private boolean isDarkMode; // Tracks whether dark mode is enabled
    private final Color LIGHT_MODE_BACKGROUND = new Color(240, 240, 240), DARK_MODE_BACKGROUND = Color.DARK_GRAY; // Background colors

    // Constructor: Initializes the game window and components
    public NumberGuessingGame() {
        setTitle("GUESS MY NUMBER"); // Set window title
        setSize(600, 350); // Set window size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close the application when the window is closed
        setLayout(null); // Use absolute positioning for components
        getContentPane().setBackground(LIGHT_MODE_BACKGROUND); // Set initial background color

        // Create and add labels for best score, guesses left, and round number
        bestScoreLabel = createLabel("🏆 Best Score: 0", 50, 25, 150, 20);
        guessesLeftLabel = createLabel("🎯 Guesses Left: " + maxAttempts, 400, 25, 150, 20);
        roundLabel = createLabel("", 220, 5, 150, 20);
        roundLabel.setHorizontalAlignment(SwingConstants.CENTER);
        updateRoundLabel(); // Update the round label text

        // Create and add the heading label
        headingLabel = new JLabel("Guess my number Game", SwingConstants.CENTER);
        headingLabel.setBounds(150, 50, 300, 30);
        headingLabel.setForeground(Color.RED);
        headingLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        add(headingLabel);

        // Load and add images for visual appeal
        leftImageLabel = createImageLabel("/resources/question_mark2.png", 50, 100, 100, 100);
        rightImageLabel = createImageLabel("/resources/question_mark4.png", 450, 100, 100, 100);

        // Create and add the instruction label
        JLabel instructionLabel = new JLabel("Enter a number b/w 1-100", SwingConstants.CENTER);
        instructionLabel.setBounds(150, 100, 300, 20);
        instructionLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(instructionLabel);

        // Create and add the input field for guesses
        guessField = new JTextField();
        guessField.setBounds(275, 130, 50, 25);
        guessField.setHorizontalAlignment(JTextField.CENTER);
        guessField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(guessField);

        // Create and add the result label (displays feedback to the player)
        resultLabel = new JLabel("Try and guess the number", SwingConstants.CENTER);
        resultLabel.setBounds(150, 160, 300, 50);
        resultLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultLabel.setForeground(Color.BLACK);
        add(resultLabel);

        // Create and add buttons for actions
        guessButton = createButton("Guess", 250, 220, 100, 30);
        giveUpButton = createButton("Give up!", 150, 260, 100, 30);
        nextRoundButton = createButton("Next Round", 350, 260, 100, 30);
        darkModeToggleButton = createIconButton("/resources/moon.png", 520, 280, 50, 30);

        // Set up event listeners and start the game
        setupListeners();
        startNewGame();
        setVisible(true); // Make the window visible
    }

    // Helper method to create and configure a label
    private JLabel createLabel(String text, int x, int y, int width, int height) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, width, height);
        label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        add(label);
        return label;
    }

    // Helper method to create and configure an image label
    private JLabel createImageLabel(String path, int x, int y, int width, int height) {
        ImageIcon icon = new ImageIcon(new ImageIcon(getClass().getResource(path)).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        JLabel label = new JLabel(icon);
        label.setBounds(x, y, width, height);
        add(label);
        return label;
    }

    // Helper method to create and configure a button
    private JButton createButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setBounds(x, y, width, height);
        button.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        add(button);
        return button;
    }

    // Helper method to create and configure an icon button (e.g., dark mode toggle)
    private JButton createIconButton(String iconPath, int x, int y, int width, int height) {
        JButton button = new JButton();
        button.setIcon(new ImageIcon(new ImageIcon(getClass().getResource(iconPath)).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        button.setBounds(x, y, width, height);
        button.setBorderPainted(false); // Remove button border
        button.setContentAreaFilled(false); // Remove button background
        button.setFocusPainted(false); // Remove focus border
        add(button);
        return button;
    }

    // Set up event listeners for buttons and input field
    private void setupListeners() {
        // Add action listeners for buttons
        guessButton.addActionListener(new GuessButtonListener());
        giveUpButton.addActionListener(e -> endRound(false, true)); // End the round if the player gives up
        nextRoundButton.addActionListener(e -> startNextRound()); // Start the next round
        darkModeToggleButton.addActionListener(e -> toggleDarkMode()); // Toggle dark mode

        // Add a document listener to enable/disable the guess button based on input
        guessField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateGuessButtonState(); }
            public void removeUpdate(DocumentEvent e) { updateGuessButtonState(); }
            public void changedUpdate(DocumentEvent e) { updateGuessButtonState(); }
            private void updateGuessButtonState() {
                guessButton.setEnabled(!guessField.getText().trim().isEmpty()); // Enable button if input is not empty
            }
        });

        // Allow pressing Enter to submit a guess
        guessField.addActionListener(e -> guessButton.doClick());
        guessField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "guess");
        guessField.getActionMap().put("guess", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { guessButton.doClick(); }
        });
    }

    // Toggle between light and dark mode
    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        getContentPane().setBackground(isDarkMode ? DARK_MODE_BACKGROUND : LIGHT_MODE_BACKGROUND); // Update background color
        resultLabel.setForeground(Color.BLACK); // Ensure text remains readable
        darkModeToggleButton.setIcon(new ImageIcon(new ImageIcon(getClass().getResource(isDarkMode ? "/resources/sun.png" : "/resources/moon.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH))); // Update icon
    }

    // Start a new game (reset all game state)
    private void startNewGame() {
        currentRound = 1; // Reset to the first round
        bestScore = 0; // Reset the best score
        bestScoreLabel.setText("🏆 Best Score: 0"); // Update the best score label
        for (int i = 0; i < totalRounds; i++) {
            roundScores[i] = 0; // Reset scores for all rounds
            roundAttempts[i] = 0; // Reset attempts for all rounds
        }

        // Reset the next round button
        nextRoundButton.setText("Next Round");
        nextRoundButton.removeActionListener(nextRoundButton.getActionListeners()[0]);
        nextRoundButton.addActionListener(e -> startNextRound());

        resetRound(); // Reset the current round
    }

    // Start the next round or end the game if all rounds are completed
    private void startNextRound() {
        if (currentRound > totalRounds) {
            showGameOver(); // Show the game over screen if all rounds are completed
            return;
        }
        resetRound(); // Reset the round state
    }

    // Reset the current round (generate a new target number and reset attempts)
    private void resetRound() {
        targetNumber = new Random().nextInt(maxRange - minRange + 1) + minRange; // Generate a new target number
        attempts = 0; // Reset attempts
        guessesLeftLabel.setText("🎯 Guesses Left: " + maxAttempts); // Update the guesses left label
        resultLabel.setText("Round " + currentRound + " - Try and guess my number!"); // Update the result label
        guessField.setEnabled(true); // Enable the input field
        guessButton.setEnabled(false); // Disable the guess button (until input is entered)
        giveUpButton.setEnabled(true); // Enable the give up button
        nextRoundButton.setEnabled(false); // Disable the next round button
        guessField.setText(""); // Clear the input field
        guessField.requestFocusInWindow(); // Focus on the input field
        updateRoundLabel(); // Update the round label
    }

    // Update the round label with the current round number
    private void updateRoundLabel() {
        roundLabel.setText("<html>Round: <font color='green'>" + currentRound + "</font>/<font color='green'>" + totalRounds + "</font></html>");
    }

    // Action listener for the guess button
    private class GuessButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                int guess = Integer.parseInt(guessField.getText()); // Get the player's guess
                if (guess < minRange || guess > maxRange) { // Validate the guess
                    resultLabel.setText("<html><div style='text-align: center;'>Please enter a number between " + minRange + " and " + maxRange + ".</div></html>");
                    guessField.setText(""); // Clear the input field
                    return;
                }

                attempts++; // Increment the number of attempts
                guessesLeftLabel.setText("🎯 Guesses Left: " + (maxAttempts - attempts)); // Update the guesses left label

                if (guess == targetNumber) { // Check if the guess is correct
                    int score = 100 / attempts; // Calculate the score
                    roundScores[currentRound - 1] = score; // Store the score for the current round
                    roundAttempts[currentRound - 1] = attempts; // Store the attempts for the current round

                    if (score > bestScore) { // Update the best score if necessary
                        bestScore = score;
                        bestScoreLabel.setText("🏆 Best Score: " + bestScore);
                    }

                    resultLabel.setText("<html><div style='text-align: center;'>Congratulations! The number was " + targetNumber + ".<br>You scored " + score + " points!</div></html>");
                    endRound(true, false); // End the round (player won)
                } else {
                    resultLabel.setText("<html><div style='text-align: center;'>" + (guess < targetNumber ? "Too low!" : "Too high!") + " Try again.</div></html>");
                }

                guessField.setText(""); // Clear the input field
                guessField.requestFocusInWindow(); // Focus on the input field

                if (attempts >= maxAttempts) { // Check if the player is out of attempts
                    resultLabel.setText("<html><div style='text-align: center;'>Out of attempts! The number was " + targetNumber + ".</div></html>");
                    endRound(false, false); // End the round (player lost)
                }
            } catch (NumberFormatException ex) { // Handle invalid input
                resultLabel.setText("<html><div style='text-align: center;'>Invalid input! Enter a number.</div></html>");
                guessField.setText(""); // Clear the input field
            }
        }
    }

    // End the current round (won or lost)
    private void endRound(boolean won, boolean gaveUp) {
        guessField.setEnabled(false); // Disable the input field
        guessButton.setEnabled(false); // Disable the guess button
        giveUpButton.setEnabled(false); // Disable the give up button

        roundAttempts[currentRound - 1] = maxAttempts - Integer.parseInt(guessesLeftLabel.getText().replaceAll("[^0-9]", "")); // Store the attempts used

        if (currentRound == totalRounds) { // Check if it's the last round
            nextRoundButton.setText("Results"); // Change the button text to "Results"
            nextRoundButton.removeActionListener(nextRoundButton.getActionListeners()[0]); // Remove the old action listener
            nextRoundButton.addActionListener(e -> showGameOver()); // Add a new action listener to show the game over screen
        }
        nextRoundButton.setEnabled(true); // Enable the next round button

        if (!won) { // If the player lost
            resultLabel.setText("Round " + currentRound + " Over! Number was " + targetNumber);
        }

        if (currentRound < totalRounds) { // Increment the round counter if not the last round
            currentRound++;
        }
    }

    // Show the game over screen with round results
    private void showGameOver() {
        JDialog gameOverDialog = new JDialog(this, "", true); // Create a dialog for the game over screen
        gameOverDialog.setSize(400, 300); // Set the dialog size
        gameOverDialog.setLayout(new BorderLayout()); // Use a border layout
        gameOverDialog.setLocationRelativeTo(this); // Center the dialog
        gameOverDialog.setUndecorated(true); // Remove the title bar
        gameOverDialog.getContentPane().setBackground(isDarkMode ? DARK_MODE_BACKGROUND : LIGHT_MODE_BACKGROUND); // Set the background color

        // Create and add the game over title
        JLabel gameOverLabel = new JLabel("Game Over! 🎮 ", SwingConstants.CENTER);
        gameOverLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        gameOverLabel.setForeground(Color.RED);
        gameOverLabel.setBorder(BorderFactory.createEmptyBorder(35, 5, 10, 0));
        gameOverDialog.add(gameOverLabel, BorderLayout.NORTH);

        // Create a panel for the center content (image and results)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add padding

        // Load and add the question mark image
        ImageIcon questionMarkIcon = new ImageIcon(getClass().getResource("/resources/question_mark.png"));
        JLabel imageLabel = new JLabel(new ImageIcon(questionMarkIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH)));

        // Create a panel for the round results
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));

        // Add results for each round
        for (int i = 0; i < totalRounds; i++) {
            JLabel roundResultLabel = new JLabel("<html><b>Round " + (i + 1) + ": </b> Score = " + roundScores[i] + ", Attempts = " + roundAttempts[i] + "</html>");
            roundResultLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            roundResultLabel.setForeground(Color.BLACK);
            resultsPanel.add(roundResultLabel);
        }

        // Add the image and results panel to the center panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(imageLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        centerPanel.add(resultsPanel, gbc);

        gameOverDialog.add(centerPanel, BorderLayout.CENTER);

        // Create a panel for the buttons (restart and close)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add the restart button
        JButton restartButton = new JButton("Restart Game");
        restartButton.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        restartButton.addActionListener(e -> {
            gameOverDialog.dispose(); // Close the dialog
            startNewGame(); // Restart the game
        });
        buttonPanel.add(restartButton);

        // Add the close button
        JButton closeButton = new JButton("Close Game");
        closeButton.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        closeButton.addActionListener(e -> {
            gameOverDialog.dispose(); // Close the dialog
            dispose(); // Close the game window
        });
        buttonPanel.add(closeButton);

        gameOverDialog.add(buttonPanel, BorderLayout.SOUTH); // Add the button panel to the dialog
        gameOverDialog.setVisible(true); // Make the dialog visible
    }

    // Main method: Entry point of the application
    public static void main(String[] args) {
        new NumberGuessingGame(); // Create and start the game
    }
}
