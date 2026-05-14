package com.elliottandcoachgeorge.javafxtest.Controllers;

import com.elliottandcoachgeorge.javafxtest.GameMode;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 *
 * In JavaFX (the UI framework being used), you always have two files working together:
 *   1. An .fxml file  — describes WHAT the screen LOOKS like (buttons, labels, layout)
 *   2. A Controller   — describes WHAT HAPPENS when the user interacts with those elements
 * This class is that controller. It handles everything: typing letters, checking guesses,
 * animating tiles, switching themes, and showing win/lose popups.
 */
public class RahkEmController {

    // =========================================================
    // GRID LABELS
    // =========================================================
    // @FXML means JavaFX will automatically connect this Java variable
    // to the matching element in the .fxml layout file (matched by the variable name).
    // These Labels are the 30 tiles on the board: 6 rows x 5 columns.
    // Naming convention: r0c0 = Row 0, Column 0 (top-left tile)
    @FXML private Label r0c0; @FXML private Label r0c1; @FXML private Label r0c2; @FXML private Label r0c3; @FXML private Label r0c4;
    @FXML private Label r1c0; @FXML private Label r1c1; @FXML private Label r1c2; @FXML private Label r1c3; @FXML private Label r1c4;
    @FXML private Label r2c0; @FXML private Label r2c1; @FXML private Label r2c2; @FXML private Label r2c3; @FXML private Label r2c4;
    @FXML private Label r3c0; @FXML private Label r3c1; @FXML private Label r3c2; @FXML private Label r3c3; @FXML private Label r3c4;
    @FXML private Label r4c0; @FXML private Label r4c1; @FXML private Label r4c2; @FXML private Label r4c3; @FXML private Label r4c4;
    @FXML private Label r5c0; @FXML private Label r5c1; @FXML private Label r5c2; @FXML private Label r5c3; @FXML private Label r5c4;

    // =========================================================
    // KEYBOARD BUTTONS
    // =========================================================
    // These are the 26 on-screen letter buttons (the clickable keyboard UI).
    // They're also @FXML injected — JavaFX wires them up automatically.
    @FXML private Button qButton; @FXML private Button wButton; @FXML private Button eButton;
    @FXML private Button rButton; @FXML private Button tButton; @FXML private Button yButton;
    @FXML private Button uButton; @FXML private Button iButton; @FXML private Button oButton;
    @FXML private Button pButton;
    @FXML private Button aButton; @FXML private Button sButton; @FXML private Button dButton;
    @FXML private Button fButton; @FXML private Button gButton; @FXML private Button hButton;
    @FXML private Button jButton; @FXML private Button kButton; @FXML private Button lButton;
    @FXML private Button zButton; @FXML private Button xButton; @FXML private Button cButton;
    @FXML private Button vButton; @FXML private Button bButton; @FXML private Button nButton;
    @FXML private Button mButton;

    // =========================================================
    // SIDE BUTTONS
    // =========================================================
    // These are the utility buttons on the side panel of the game screen.
    @FXML private Button submitButton;   // submits the current guess
    @FXML private Button clearButton;    // clears the current row
    @FXML private Button restartButton;  // starts a new game
    @FXML private Button exitButton;     // closes the application
    @FXML private Button backButton;     // returns to the main menu
    @FXML private ImageView logoImage;   // displays a subject-specific logo image
    @FXML private BorderPane rootPane;   // the root layout container for the whole screen
    // BorderPane divides the screen into: top, bottom, left, right, center

    // =========================================================
    // GAME VARIABLES
    // =========================================================

    // A 2D array (a grid) of Label objects representing the game board.
    // Think of it like a spreadsheet: board[row][column] gives you one tile.
    // board[0][0] is the top-left tile; board[5][4] is the bottom-right tile.
    private Label[][] board;

    // HashMap is a data structure that stores key-value pairs, like a dictionary.
    // Here: the KEY is a String like "A", and the VALUE is the Button for that letter.
    // This lets us quickly look up a button by its letter name — e.g. keyboardMap.get("A") → aButton.
    private final HashMap<String, Button> keyboardMap = new HashMap<>();

    // Tracks which row (guess attempt) and which column (letter slot) the player is on.
    // currentRow goes from 0 (first guess) to 5 (sixth and final guess).
    // currentCol goes from 0 (first letter) to 4 (fifth letter).
    private int currentRow = 0;
    private int currentCol = 0;

    // An array of all valid 5-letter words loaded from words.txt.
    // Used to pick a random or daily target word.
    private String[] WORDS;

    // HashSet is like a bag of unique items with extremely fast lookup.
    // We use it as the dictionary so we can instantly check if a guess is a real word.
    // Why not use WORDS[] for this? Searching an array takes O(n) time (checks every item).
    // A HashSet lookup is O(1) — it jumps directly to the answer, no matter how large.
    private final HashSet<String> dictionary = new HashSet<>();

    // The word the player is trying to guess. Set on game start.
    private String targetWord;

    // Tracks whether the player has submitted at least one valid guess.
    // (Currently declared but could be used for stats or UI logic.)
    private boolean hasSubmitted = false;

    // GameMode is a custom enum (a fixed list of named options) defined elsewhere in the project.
    // It can be FREE_PLAY (random word each game) or DAILY (same word for everyone that day).
    private GameMode gameMode = GameMode.FREE_PLAY;

    // A Runnable is a simple Java interface that holds a block of code to run later.
    // Here it's used as a "callback" — when the back button is pressed, we run
    // whatever code was assigned here by the calling screen (like navigating back to a menu).
    // This keeps navigation logic out of this controller — it doesn't need to know
    // anything about the menu screen, just that it should "run this action" when back is pressed.
    private Runnable backCallback;

    // Stores the name of the current color theme. Defaults to "Dark".
    private String currentTheme = "Dark";

    // =========================================================
    // SETTERS
    // =========================================================
    // These public methods let other parts of the app configure this controller
    // after it's created (e.g., the main menu sets the game mode before showing the screen).

    /**
     * Sets the game mode (FREE_PLAY or DAILY) and picks the target word accordingly.
     */
    public void setGameMode(GameMode mode) {
        this.gameMode = mode;
        // Choose the word based on which mode we're in
        if (mode == GameMode.FREE_PLAY) {
            targetWord = getRandomWord();
        } else {
            targetWord = getDailyWord();
        }
        // Apply the board tile styling after the mode is set
        setupBoardStyle();
    }

    /**
     * Stores a callback (a Runnable) to be executed when the back button is pressed.
     * The calling code passes in the navigation action here so this controller
     * doesn't need to know about other screens.
     */
    public void setBackCallback(Runnable callback) {
        this.backCallback = callback;
    }

    /**
     * Allows an external class to change the visual theme of this screen.
     * Updates the stored theme name and refreshes the board appearance.
     */
    public void applyThemeExternal(String theme) {
        currentTheme = theme;
        applyTheme(theme);
        setupBoardStyle();
    }

    // =========================================================
    // INITIALIZE
    // =========================================================
    /**
     * initialize() is automatically called by JavaFX right after the .fxml file is loaded.
     * Think of it as the constructor for the screen — this is where you wire everything up.
     * The @FXML variables are guaranteed to be set by the time this method runs.
     */
    @FXML
    public void initialize() {
        // Build the 2D board array from the individually named Label fields.
        // This is much easier than writing board[0][0] to access r0c0 manually every time.
        board = new Label[][]{
                {r0c0, r0c1, r0c2, r0c3, r0c4},
                {r1c0, r1c1, r1c2, r1c3, r1c4},
                {r2c0, r2c1, r2c2, r2c3, r2c4},
                {r3c0, r3c1, r3c2, r3c3, r3c4},
                {r4c0, r4c1, r4c2, r4c3, r4c4},
                {r5c0, r5c1, r5c2, r5c3, r5c4}
        };

        // Populate the keyboardMap so we can look up any button by its letter
        setupKeyboardMap();

        // Read words.txt and fill WORDS[] and dictionary
        loadWords();

        // Pick the first target word (random by default; may be overridden by setGameMode)
        targetWord = getRandomWord();

        // Wire up button click handlers using lambda expressions.
        // A lambda (e -> ...) is a short way to write a small anonymous function.
        // "e" is the event object (we don't use it here, but it's required by the interface).
        submitButton.setOnAction(e -> submitGuess());
        clearButton.setOnAction(e -> clearRow());
        restartButton.setOnAction(e -> restartGame());
        exitButton.setOnAction(e -> System.exit(0));  // System.exit(0) closes the whole app
        backButton.setOnAction(e -> handleBack());

        // addEventFilter listens for key events on the rootPane (the whole screen).
        // KEY_TYPED fires when a printable character is typed (letters, numbers, etc.)
        // This handles physical keyboard input for typing letters.
        rootPane.addEventFilter(KeyEvent.KEY_TYPED, this::handleKeyTyped);

        // setOnKeyPressed listens for special keys like Enter and Backspace,
        // which don't generate a KEY_TYPED event in the same way.
        rootPane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                submitGuess();
            }
            if (e.getCode() == KeyCode.BACK_SPACE) {
                deleteLetter();
            }
        });

        // Make the rootPane able to receive keyboard focus
        rootPane.setFocusTraversable(true);

        // Platform.runLater schedules this to run after the screen is fully rendered.
        // We need this because requesting focus before the scene is shown doesn't work.
        Platform.runLater(() -> rootPane.requestFocus());
    }

    // =========================================================
    // KEYBOARD MAP
    // =========================================================
    /**
     * Populates the keyboardMap HashMap so that any letter string like "A"
     * maps directly to its corresponding on-screen Button (aButton).
     * This lets us update button colors after each guess without 26 if-statements.
     */
    private void setupKeyboardMap() {
        keyboardMap.put("Q", qButton);
        keyboardMap.put("W", wButton);
        keyboardMap.put("E", eButton);
        keyboardMap.put("R", rButton);
        keyboardMap.put("T", tButton);
        keyboardMap.put("Y", yButton);
        keyboardMap.put("U", uButton);
        keyboardMap.put("I", iButton);
        keyboardMap.put("O", oButton);
        keyboardMap.put("P", pButton);
        keyboardMap.put("A", aButton);
        keyboardMap.put("S", sButton);
        keyboardMap.put("D", dButton);
        keyboardMap.put("F", fButton);
        keyboardMap.put("G", gButton);
        keyboardMap.put("H", hButton);
        keyboardMap.put("J", jButton);
        keyboardMap.put("K", kButton);
        keyboardMap.put("L", lButton);
        keyboardMap.put("Z", zButton);
        keyboardMap.put("X", xButton);
        keyboardMap.put("C", cButton);
        keyboardMap.put("V", vButton);
        keyboardMap.put("B", bButton);
        keyboardMap.put("N", nButton);
        keyboardMap.put("M", mButton);
    }

    // =========================================================
    // BOARD STYLE
    // =========================================================
    /**
     * Applies the default (unguessed) tile appearance to every tile on the board.
     * Called at startup and after a restart to reset the board visually.
     * Tile colors come from the current theme via getThemeTileColor().
     */
    private void setupBoardStyle() {
        // Safety check: if the board hasn't been built yet, do nothing
        if (board == null) return;

        String backgroundColor = getThemeTileColor();

        // Loop through all 6 rows and 5 columns
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 5; c++) {
                // setStyle applies inline CSS styling to a JavaFX node (similar to HTML inline styles)
                board[r][c].setStyle(
                        "-fx-background-color:" + backgroundColor + ";" +
                                "-fx-background-radius:6;" +   // rounded corners
                                "-fx-font-size:34;" +
                                "-fx-font-family:'Arial';" +
                                "-fx-font-weight:bold;" +
                                "-fx-text-fill:white;" +
                                "-fx-alignment:center;"
                );
            }
        }
    }

    /**
     * Returns the hex color string for unguessed tiles based on the current theme name.
     * Each theme has a distinctive background color for the board tiles.
     */
    private String getThemeTileColor() {
        // A switch statement checks the value of currentTheme and returns the matching color.
        // "default" is the fallback if none of the cases match.
        switch (currentTheme) {
            case "WSA":      return "#0a1a5c";
            case "Dark":     return "#2f2f2f";
            case "Light":    return "#d3d6da";
            case "Blue":     return "#4d79ff";
            case "Ocean":    return "#006994";
            case "Raven":    return "#3b3b58";
            case "Coach":    return "getThemeTileColor";
            case "Willow":   return "#4f7942";
            case "Fuschia":  return "#c154c1";
            case "Bell":     return "getThemeTileColor";
            case "The Four": return "getThemeTileColor";
            case "Storm":    return "#090913";
            case "Orange":   return "#ff8c00";
            default:         return "#2f2f2f";
        }
    }

    // =========================================================
    // BACK BUTTON
    // =========================================================
    /**
     * Called when the back button is pressed.
     * Runs the backCallback if one was provided (it's set by the calling screen).
     * If no callback was set, nothing happens — the null check prevents a crash.
     */
    @FXML
    private void handleBack() {
        if (backCallback != null) {
            backCallback.run(); // execute whatever navigation logic was passed in
        }
    }

    // =========================================================
    // LOAD WORDS
    // =========================================================
    /**
     * Reads words from the file /resources/words.txt and stores them in:
     *   - WORDS[]      → for picking a random or daily word
     *   - dictionary   → for fast validation of guesses
     *
     * Only 5-letter words are accepted. If the file can't be found,
     * it falls back to "LUCAS" so the game still works.
     */
    private void loadWords() {
        ArrayList<String> wordList = new ArrayList<>(); // a growable list of words

        try {
            // getResourceAsStream looks for a file inside the compiled resources folder.
            // Returns null if the file doesn't exist, so we check for that.
            InputStream inputStream = getClass().getResourceAsStream("/words.txt");

            if (inputStream == null) {
                // File not found — use a single fallback word
                wordList.add("LUCAS");
                dictionary.add("LUCAS");
            } else {
                // BufferedReader wraps the InputStream to let us read it line by line efficiently.
                // InputStreamReader converts the raw byte stream to text characters.
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                // readLine() returns null when we reach the end of the file
                while ((line = reader.readLine()) != null) {
                    line = line.trim().toUpperCase(); // remove whitespace, force uppercase
                    if (line.length() == 5) {         // only keep 5-letter words
                        wordList.add(line);           // add to the array list (for random picks)
                        dictionary.add(line);         // add to the set (for fast lookup)
                    }
                }
                reader.close(); // always close streams when done to free memory
            }
        } catch (Exception e) {
            // If anything goes wrong (IO error, etc.) print the error and use fallback word
            e.printStackTrace();
            wordList.add("LUCAS");
            dictionary.add("LUCAS");
        }

        // Convert the ArrayList to a plain array — WORDS is used for index-based access
        // (ArrayList requires .get(i), while arrays use [i], which is simpler here)
        WORDS = wordList.toArray(new String[0]);
    }

    // =========================================================
    // WORD SELECTION
    // =========================================================
    /**
     * Returns a random word from the WORDS array.
     * new Random().nextInt(WORDS.length) generates a random int from 0 to length-1.
     */
    private String getRandomWord() {
        if (WORDS == null || WORDS.length == 0) {
            return "LUCAS"; // safety fallback
        }
        return WORDS[new Random().nextInt(WORDS.length)];
    }

    /**
     * Returns a deterministic "word of the day" — the same word for everyone on the same date.
     *
     * How it works:
     *   1. Pick a fixed start date (Jan 1, 2026)
     *   2. Count how many days have passed since then
     *   3. Use that count as an index into the WORDS array (wrapping with %)
     *
     * Because every device sees the same current date and the same WORDS array,
     * everyone gets the same word. The % (modulo) operator wraps the index so
     * it never goes out of bounds — e.g., 10000 % 2500 = 0.
     */
    private String getDailyWord() {
        if (WORDS == null || WORDS.length == 0) {
            return "LUCAS"; // safety fallback
        }
        LocalDate start = LocalDate.of(2026, 1, 1);
        long days = ChronoUnit.DAYS.between(start, LocalDate.now()); // number of days elapsed
        int index = (int) (Math.abs(days) % WORDS.length);           // keep index in bounds
        return WORDS[index];
    }

    // =========================================================
    // INPUT
    // =========================================================
    /**
     * Handles a KEY_TYPED event from the physical keyboard.
     * KEY_TYPED fires for printable characters (letters, numbers, symbols).
     *
     * We filter to only accept A–Z using a regex (regular expression):
     *   [A-Z] means "any single character from A to Z"
     *   matches() returns true if the string fits that pattern
     */
    private void handleKeyTyped(KeyEvent event) {
        String character = event.getCharacter().toUpperCase();
        if (!character.matches("[A-Z]")) {
            event.consume(); // consume() stops the event from propagating further
            return;
        }
        addLetter(character);
    }

    /**
     * Places a letter on the current tile and advances the column cursor.
     * Does nothing if the row is already full (currentCol >= 5).
     */
    private void addLetter(String letter) {
        if (currentCol < 5) {
            Label tile = board[currentRow][currentCol]; // get the current tile
            tile.setText(letter);                       // display the letter
            playPopAnimation(tile);                     // play a small scale-up animation
            currentCol++;                               // move the cursor to the next column
        }
    }

    /**
     * Removes the last typed letter by moving the cursor back one column
     * and clearing that tile's text. Does nothing if the row is already empty.
     */
    private void deleteLetter() {
        if (currentCol > 0) {
            currentCol--;                              // move cursor back
            board[currentRow][currentCol].setText(""); // clear the tile
        }
    }

    /**
     * Clears all letters from the current row and resets the column cursor to 0.
     */
    private void clearRow() {
        for (int i = 0; i < 5; i++) {
            board[currentRow][i].setText("");
        }
        currentCol = 0;
    }

    // =========================================================
    // POP ANIMATION
    // =========================================================
    /**
     * Plays a quick "pop" scale animation on a tile when a letter is typed.
     *
     * ScaleTransition smoothly changes the size of a node over time.
     * Here it scales from 1.0x (normal) to 1.15x (slightly bigger) and back.
     *
     * setAutoReverse(true) makes it animate back to the start automatically.
     * setCycleCount(2) means: go forward once, then reverse once = 1 full pop.
     */
    private void playPopAnimation(Label tile) {
        ScaleTransition pop = new ScaleTransition(Duration.millis(100), tile);
        pop.setFromX(1.0);  // starting horizontal scale (normal)
        pop.setFromY(1.0);  // starting vertical scale (normal)
        pop.setToX(1.15);   // target horizontal scale (slightly enlarged)
        pop.setToY(1.15);   // target vertical scale (slightly enlarged)
        pop.setAutoReverse(true);
        pop.setCycleCount(2);
        pop.play(); // start the animation
    }

    // =========================================================
    // SHAKE ROW
    // =========================================================
    /**
     * Plays a "shake" animation on the tiles of a row to signal an invalid word.
     * Each tile bounces up then down sequentially (one after the other, not all at once).
     *
     * SequentialTransition runs its children one after another in order.
     * TranslateTransition moves a node by a relative offset (setByY moves up/down).
     *   Negative Y = up (screen coordinates are top-down: 0 is the top).
     */
    private void shakeRow(int row) {
        SequentialTransition sequence = new SequentialTransition();

        for (int c = 0; c < 5; c++) {
            Label tile = board[row][c];

            // Move tile up by 18 pixels over 80ms
            TranslateTransition bounceUp = new TranslateTransition(Duration.millis(80), tile);
            bounceUp.setByY(-18);

            // Move tile back down by 18 pixels over 80ms
            TranslateTransition bounceDown = new TranslateTransition(Duration.millis(80), tile);
            bounceDown.setByY(18);

            // Snap the tile back to its exact original position (Y=0)
            // setByY is relative; setToY is absolute — ensures no drift
            TranslateTransition returnHome = new TranslateTransition(Duration.millis(80), tile);
            returnHome.setByY(0);
            returnHome.setToY(0);

            // Chain the three transitions for this tile into a mini-sequence
            SequentialTransition tileBounce = new SequentialTransition(bounceUp, bounceDown, returnHome);
            sequence.getChildren().add(tileBounce); // add this tile's bounce to the overall sequence
        }

        sequence.play(); // start the whole sequential shake
    }

    // =========================================================
    // SUBMIT GUESS
    // =========================================================
    /**
     * The core game logic. Called when the player presses Enter or the Submit button.
     *
     * Steps:
     *   1. Ensure 5 letters have been typed
     *   2. Validate the word against the dictionary
     *   3. For each letter, animate a flip and color the tile:
     *        - Green  = correct letter, correct position
     *        - Yellow = correct letter, wrong position
     *        - Gray   = letter not in the word at all
     *   4. Update the on-screen keyboard buttons to match
     *   5. After animations finish, check for win or lose
     *
     * The used[] boolean array is critical for correct coloring with repeated letters.
     * Example: target = "BELLE", guess = "LLAMA"
     *   - First L: is there an unused L in BELLE? Yes (position 2). Mark it yellow. used[2] = true.
     *   - Second L: is there another unused L? No. Mark it gray.
     * Without tracking used[], both L's would turn yellow, which is wrong.
     */
    private void submitGuess() {
        // Don't submit if the row isn't completely filled
        if (currentCol < 5) {
            return;
        }

        // Build the guess string from the current row's tile texts
        StringBuilder guessBuilder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            guessBuilder.append(board[currentRow][i].getText());
        }
        String guess = guessBuilder.toString();

        // Check if the guess is in the dictionary (a valid word)
        if (!dictionary.contains(guess)) {
            shakeRow(currentRow); // shake the row to signal "not a valid word"
            return;
        }

        hasSubmitted = true;

        // Tracks which target-word positions have already been "claimed" by a matching guess letter.
        // Prevents the same target letter from being counted twice (see method Javadoc above).
        boolean[] used = new boolean[5];

        // Animate and color each tile with a staggered delay (250ms per tile)
        for (int i = 0; i < 5; i++) {
            Label tile = board[currentRow][i];
            String guessLetter = guess.substring(i, i + 1);   // one letter from the guess
            String targetLetter = targetWord.substring(i, i + 1); // matching letter from the target
            final int index = i; // must be final to use inside a lambda (Java requires this)

            // PauseTransition waits a set duration, then fires setOnFinished.
            // Each tile waits an extra 250ms more than the previous one (i * 250).
            PauseTransition delay = new PauseTransition(Duration.millis(i * 250));
            delay.setOnFinished(e -> {
                // First half of flip: rotate the tile from 0° to 90° around the X axis.
                // At 90° the tile is "edge-on" — invisible from the front — so we change
                // the color here, then finish the rotation to reveal the new color.
                RotateTransition flip = new RotateTransition(Duration.millis(300), tile);
                flip.setAxis(Rotate.X_AXIS);  // rotate around the horizontal axis (like a card flip)
                flip.setFromAngle(0);
                flip.setToAngle(90);

                flip.setOnFinished(ev -> {
                    // Tile is now "face-down" (edge-on). Apply the correct color.
                    if (guessLetter.equals(targetLetter)) {
                        // Letter is in the right spot — green
                        used[index] = true;
                        setTileGreen(tile);
                        updateKeyboardColor(guessLetter, "green");
                    } else {
                        // Check if this letter appears anywhere else in the target (that hasn't been used yet)
                        boolean found = false;
                        for (int j = 0; j < 5; j++) {
                            if (!used[j] && guessLetter.equals(targetWord.substring(j, j + 1))) {
                                used[j] = true; // claim this target position so it's not matched again
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            setTileYellow(tile);                   // letter exists but in wrong position
                            updateKeyboardColor(guessLetter, "yellow");
                        } else {
                            setTileGray(tile);                     // letter not in the word
                            updateKeyboardColor(guessLetter, "gray");
                        }
                    }

                    // Second half of flip: rotate from 90° back to 0° to reveal the colored tile
                    RotateTransition finishFlip = new RotateTransition(Duration.millis(300), tile);
                    finishFlip.setAxis(Rotate.X_AXIS);
                    finishFlip.setFromAngle(90);
                    finishFlip.setToAngle(0);
                    finishFlip.play();
                });

                flip.play(); // start the first half of the flip
            });

            delay.play(); // start the staggered delay for this tile
        }

        // Wait for all tile animations to finish before checking the game result.
        // 5 tiles × 250ms stagger + 600ms flip duration = ~1850ms total.
        // We wait 1600ms (a close approximation that feels right in practice).
        PauseTransition end = new PauseTransition(Duration.millis(1600));
        end.setOnFinished(e -> {
            if (guess.equals(targetWord)) {
                showWinWindow(); // player guessed correctly!
            } else {
                currentRow++; // move to the next row
                currentCol = 0;
                if (currentRow >= 6) {
                    showLoseWindow(); // no more guesses left
                }
            }
        });
        end.play();
    }

    // =========================================================
    // TILE COLORS
    // =========================================================
    // Each method below sets a tile's background to the Wordle-standard colors.
    // The styles are written as inline CSS strings — same format as in web development.

    /** Colors a tile green (correct letter, correct position). */
    private void setTileGreen(Label tile) {
        tile.setStyle(
                "-fx-background-color:#6aaa64;" + // Wordle green
                        "-fx-background-radius:6;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:34;" +
                        "-fx-font-family:'Arial';" +
                        "-fx-font-weight:bold;"
        );
    }

    /** Colors a tile yellow (correct letter, wrong position). */
    private void setTileYellow(Label tile) {
        tile.setStyle(
                "-fx-background-color:#c9b458;" + // Wordle yellow/gold
                        "-fx-background-radius:6;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:34;" +
                        "-fx-font-family:'Arial';" +
                        "-fx-font-weight:bold;"
        );
    }

    /** Colors a tile gray (letter not in the word). */
    private void setTileGray(Label tile) {
        tile.setStyle(
                "-fx-background-color:#787c7e;" + // Wordle gray
                        "-fx-background-radius:6;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:34;" +
                        "-fx-font-family:'Arial';" +
                        "-fx-font-weight:bold;"
        );
    }

    // =========================================================
    // KEYBOARD COLORS
    // =========================================================
    /**
     * Updates an on-screen keyboard button's color to reflect what has been learned
     * about that letter so far. Follows a strict priority rule:
     *
     *   Green  > Yellow > Gray
     *
     * A key that is already green can never be downgraded.
     * A key that is yellow cannot be downgraded to gray (the letter IS in the word somewhere).
     * Only the "best" (most informative) color is kept.
     */
    private void updateKeyboardColor(String letter, String color) {
        Button key = keyboardMap.get(letter); // look up the button for this letter
        if (key == null) {
            return; // shouldn't happen, but guard against a missing entry
        }

        String current = key.getStyle(); // read the button's current CSS style string

        // Don't override green — a correct guess is final
        if (current.contains("#6aaa64")) {
            return;
        }
        // Don't downgrade yellow to gray — the letter is still in the word
        if (current.contains("#c9b458") && color.equals("gray")) {
            return;
        }

        // Apply the new color
        switch (color) {
            case "green":
                key.setStyle(
                        "-fx-background-color:#6aaa64;" +
                                "-fx-text-fill:white;" +
                                "-fx-font-weight:bold;"
                );
                break;
            case "yellow":
                key.setStyle(
                        "-fx-background-color:#c9b458;" +
                                "-fx-text-fill:white;" +
                                "-fx-font-weight:bold;"
                );
                break;
            case "gray":
                key.setStyle(
                        "-fx-background-color:#787c7e;" +
                                "-fx-text-fill:white;" +
                                "-fx-font-weight:bold;"
                );
                break;
        }
    }

    // =========================================================
    // RESTART
    // =========================================================
    /**
     * Resets the entire game to its initial state:
     *   - Picks a new target word
     *   - Clears all tile text and colors
     *   - Resets keyboard button colors
     *   - Resets row/column cursors
     *   - Returns keyboard focus to the root pane so typing works again
     */
    private void restartGame() {
        currentRow = 0;
        currentCol = 0;

        // Pick the next word based on the current game mode
        if (gameMode == GameMode.FREE_PLAY) {
            targetWord = getRandomWord();
        } else {
            targetWord = getDailyWord();
        }

        setupBoardStyle(); // reset tile colors to the default theme color

        // Reset all keyboard button styles to default (blank)
        for (Button button : keyboardMap.values()) {
            button.setStyle("");
        }

        // Clear all tile text
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 5; c++) {
                board[r][c].setText("");
            }
        }

        rootPane.requestFocus(); // re-enable keyboard input
    }

    // =========================================================
    // THEME
    // =========================================================
    /**
     * Switches the visual theme of the game screen.
     * Clears any previously loaded CSS stylesheets and reapplies board tile colors.
     *
     * scene.getStylesheets() holds a list of CSS file paths that style the scene.
     * Clearing it removes any previously applied theme stylesheet.
     */
    private void applyTheme(String theme) {
        currentTheme = theme;
        Scene scene = rootPane.getScene();
        if (scene == null) {
            return; // scene might not be set yet during initialization — skip safely
        }
        scene.getStylesheets().clear(); // remove old theme stylesheets
        setupBoardStyle();              // re-apply tile colors for the new theme
    }

    // =========================================================
    // SUBJECT / IMAGE
    // =========================================================
    /**
     * Loads and displays a subject-specific logo image into the logoImage ImageView.
     * The image file is expected at /resources/Images/<subject>.png (all lowercase).
     *
     * If the image file isn't found, we silently skip it — the default logo stays.
     */
    public void setSubject(String subject) {
        if (logoImage == null) return;
        String imageName = subject.toLowerCase() + ".png";
        try {
            javafx.scene.image.Image img = new javafx.scene.image.Image(
                    getClass().getResourceAsStream("/Images/" + imageName)
            );
            logoImage.setImage(img);
        } catch (Exception e) {
            // Image not found — print a debug message but don't crash
            System.out.println("Image not found: " + imageName);
        }
    }

    // =========================================================
    // WIN / LOSE WINDOWS
    // =========================================================
    /**
     * Opens a new popup window celebrating the player's win.
     *
     * Stage  = a window in JavaFX (the main window is also a Stage)
     * VBox   = a layout container that stacks children vertically
     * HBox   = a layout container that places children horizontally side-by-side
     * Scene  = the content inside a Stage (each Stage shows one Scene at a time)
     *
     * The popup is built entirely in code (no .fxml file) and includes:
     *   - A "YOU WIN!" heading
     *   - The correct word displayed in green
     *   - Restart and Exit buttons
     */
    private void showWinWindow() {
        Stage winStage = new Stage(); // create a new window

        Label winLabel = new Label("YOU WIN!");
        winLabel.setStyle(
                "-fx-font-size:48;" +
                        "-fx-font-weight:bold;" +
                        "-fx-text-fill:white;"
        );

        // Display the target word so the player can see what they guessed
        Label wordLabel = new Label(targetWord);
        wordLabel.setStyle(
                "-fx-font-size:22;" +
                        "-fx-font-weight:bold;" +
                        "-fx-text-fill:#6aaa64;" +          // green text
                        "-fx-background-color:#2f2f2f;" +
                        "-fx-padding:14 28 14 28;" +         // padding: top/bottom left/right
                        "-fx-background-radius:8;"
        );

        // Restart button — closes this popup and starts a new game
        Button restartBtn = new Button("RESTART");
        restartBtn.setStyle(
                "-fx-font-size:16;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-color:#444444;" +
                        "-fx-text-fill:white;" +
                        "-fx-padding:10 24 10 24;" +
                        "-fx-background-radius:6;"
        );
        restartBtn.setOnAction(e -> {
            restartGame();
            winStage.close(); // close the popup
        });

        Button exitBtn = new Button("EXIT");
        exitBtn.setStyle(
                "-fx-font-size:16;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-color:#444444;" +
                        "-fx-text-fill:white;" +
                        "-fx-padding:10 24 10 24;" +
                        "-fx-background-radius:6;"
        );
        exitBtn.setOnAction(e -> System.exit(0)); // exit the whole app

        // HBox places the two buttons side by side with 16px spacing
        HBox buttonBox = new HBox(16, restartBtn, exitBtn);
        buttonBox.setAlignment(Pos.CENTER); // center the buttons horizontally

        // VBox stacks all elements vertically with 24px spacing between them
        VBox layout = new VBox(24, winLabel, wordLabel, buttonBox);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color:black; -fx-padding:40;");

        // Build the scene (content) and assign it to the new window
        Scene scene = new Scene(layout, 340, 260);
        winStage.setScene(scene);
        winStage.setTitle("You Win!");
        winStage.show(); // display the window
    }

    /**
     * Opens a new popup window showing the player they've lost.
     * Same structure as showWinWindow() but with red styling and the answer revealed.
     */
    private void showLoseWindow() {
        Stage loseStage = new Stage();

        Label loseLabel = new Label("YOU LOSE!");
        loseLabel.setStyle(
                "-fx-font-size:48;" +
                        "-fx-font-weight:bold;" +
                        "-fx-text-fill:white;"
        );

        // Show the correct word so the player learns what they missed
        Label wordLabel = new Label(targetWord);
        wordLabel.setStyle(
                "-fx-font-size:22;" +
                        "-fx-font-weight:bold;" +
                        "-fx-text-fill:#cc3333;" +          // red text
                        "-fx-background-color:#2f2f2f;" +
                        "-fx-padding:14 28 14 28;" +
                        "-fx-background-radius:8;"
        );

        Button restartBtn = new Button("RESTART");
        restartBtn.setStyle(
                "-fx-font-size:16;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-color:#444444;" +
                        "-fx-text-fill:white;" +
                        "-fx-padding:10 24 10 24;" +
                        "-fx-background-radius:6;"
        );
        restartBtn.setOnAction(e -> {
            restartGame();
            loseStage.close();
        });

        Button exitBtn = new Button("EXIT");
        exitBtn.setStyle(
                "-fx-font-size:16;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-color:#444444;" +
                        "-fx-text-fill:white;" +
                        "-fx-padding:10 24 10 24;" +
                        "-fx-background-radius:6;"
        );
        exitBtn.setOnAction(e -> System.exit(0));

        HBox buttonBox = new HBox(16, restartBtn, exitBtn);
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(24, loseLabel, wordLabel, buttonBox);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color:black; -fx-padding:40;");

        Scene scene = new Scene(layout, 340, 260);
        loseStage.setScene(scene);
        loseStage.setTitle("You Lose!");
        loseStage.show();
    }

    // =========================================================
    // ON-SCREEN KEYBOARD BUTTON HANDLERS
    // =========================================================
    // Each @FXML method is wired to the corresponding buttons onAction in the .fxml file
    // They all just call addLetter() with their respective letter. there is no logic here.
    @FXML private void handleQ() { addLetter("Q"); }
    @FXML private void handleW() { addLetter("W"); }
    @FXML private void handleE() { addLetter("E"); }
    @FXML private void handleR() { addLetter("R"); }
    @FXML private void handleT() { addLetter("T"); }
    @FXML private void handleY() { addLetter("Y"); }
    @FXML private void handleU() { addLetter("U"); }
    @FXML private void handleI() { addLetter("I"); }
    @FXML private void handleO() { addLetter("O"); }
    @FXML private void handleP() { addLetter("P"); }
    @FXML private void handleA() { addLetter("A"); }
    @FXML private void handleS() { addLetter("S"); }
    @FXML private void handleD() { addLetter("D"); }
    @FXML private void handleF() { addLetter("F"); }
    @FXML private void handleG() { addLetter("G"); }
    @FXML private void handleH() { addLetter("H"); }
    @FXML private void handleJ() { addLetter("J"); }
    @FXML private void handleK() { addLetter("K"); }
    @FXML private void handleL() { addLetter("L"); }
    @FXML private void handleZ() { addLetter("Z"); }
    @FXML private void handleX() { addLetter("X"); }
    @FXML private void handleC() { addLetter("C"); }
    @FXML private void handleV() { addLetter("V"); }
    @FXML private void handleB() { addLetter("B"); }
    @FXML private void handleN() { addLetter("N"); }
    @FXML private void handleM() { addLetter("M"); }
}