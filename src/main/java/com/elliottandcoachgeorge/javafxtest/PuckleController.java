package com.elliottandcoachgeorge.javafxtest;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Random;

public class PuckleController {

    // ---------------- GRID LABELS ----------------
    @FXML private Label r0c0; @FXML private Label r0c1; @FXML private Label r0c2; @FXML private Label r0c3; @FXML private Label r0c4;
    @FXML private Label r1c0; @FXML private Label r1c1; @FXML private Label r1c2; @FXML private Label r1c3; @FXML private Label r1c4;
    @FXML private Label r2c0; @FXML private Label r2c1; @FXML private Label r2c2; @FXML private Label r2c3; @FXML private Label r2c4;
    @FXML private Label r3c0; @FXML private Label r3c1; @FXML private Label r3c2; @FXML private Label r3c3; @FXML private Label r3c4;
    @FXML private Label r4c0; @FXML private Label r4c1; @FXML private Label r4c2; @FXML private Label r4c3; @FXML private Label r4c4;
    @FXML private Label r5c0; @FXML private Label r5c1; @FXML private Label r5c2; @FXML private Label r5c3; @FXML private Label r5c4;

    // ---------------- BUTTONS ----------------
    @FXML private Button submitButton;
    @FXML private Button clearButton;
    @FXML private Button restartButton;
    @FXML private Button exitButton;

    // ---------------- UI ----------------
    @FXML private ImageView logoImage;
    @FXML private ComboBox<String> themeDropdown;
    @FXML private BorderPane rootPane;

    // ---------------- GAME STATE ----------------
    private Label[][] board;
    private int currentRow = 0;
    private int currentCol = 0;

    private final String[] WORDS = {"LUCAS"};
    private String targetWord;

    private boolean hasSubmitted = false;

    // ---------------- INIT ----------------
    @FXML
    public void initialize() {

        board = new Label[][]{
                {r0c0, r0c1, r0c2, r0c3, r0c4},
                {r1c0, r1c1, r1c2, r1c3, r1c4},
                {r2c0, r2c1, r2c2, r2c3, r2c4},
                {r3c0, r3c1, r3c2, r3c3, r3c4},
                {r4c0, r4c1, r4c2, r4c3, r4c4},
                {r5c0, r5c1, r5c2, r5c3, r5c4}
        };

        targetWord = WORDS[new Random().nextInt(WORDS.length)];

        submitButton.setOnAction(e -> submitGuess());
        clearButton.setOnAction(e -> clearRow());
        restartButton.setOnAction(e -> restartGame());
        exitButton.setOnAction(e -> System.exit(0));

        themeDropdown.setOnAction(e -> applyTheme(themeDropdown.getValue()));

        rootPane.addEventFilter(KeyEvent.KEY_TYPED, this::handleKeyTyped);

        rootPane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                submitGuess();
            }
        });

        rootPane.setFocusTraversable(true);
        rootPane.requestFocus();

        themeDropdown.setValue("Light");
        applyTheme("Light");
    }

    // ---------------- INPUT ----------------
    private void handleKeyTyped(KeyEvent event) {

        String character = event.getCharacter().toUpperCase();

        if (!character.matches("[A-Z]")) {
            event.consume();
            return;
        }

        addLetter(character);
    }

    private void addLetter(String letter) {
        if (currentCol < 5) {
            board[currentRow][currentCol].setText(letter);
            currentCol++;
        }
    }

    private void clearRow() {
        for (int i = 0; i < 5; i++) {
            board[currentRow][i].setText("");
        }
        currentCol = 0;
    }

    // ---------------- GAME LOGIC ----------------
    private void submitGuess() {

        if (currentCol < 5) return;

        hasSubmitted = true;

        StringBuilder guessBuilder = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            guessBuilder.append(board[currentRow][i].getText());
        }

        String guess = guessBuilder.toString();

        for (int i = 0; i < 5; i++) {

            Label tile = board[currentRow][i];
            String letter = guess.substring(i, i + 1);

            if (targetWord.substring(i, i + 1).equals(letter)) {

                tile.setStyle("-fx-background-color:#6aaa64;-fx-text-fill:white;-fx-border-color:black;-fx-border-width:2;-fx-font-size:28;-fx-font-weight:bold;");

            } else if (targetWord.contains(letter)) {

                tile.setStyle("-fx-background-color:#c9b458;-fx-text-fill:white;-fx-border-color:black;-fx-border-width:2;-fx-font-size:28;-fx-font-weight:bold;");

            } else {

                tile.setStyle("-fx-background-color:#787c7e;-fx-text-fill:white;-fx-border-color:black;-fx-border-width:2;-fx-font-size:28;-fx-font-weight:bold;");
            }
        }

        if (guess.equals(targetWord)) {
            showWinWindow();
            return;
        }

        currentRow++;
        currentCol = 0;

        if (currentRow >= 6) {
            showLoseWindow();
        }
    }

    // ---------------- FIXED RESTART (NO WHITE RESET BUG) ----------------
    private void restartGame() {

        currentRow = 0;
        currentCol = 0;
        hasSubmitted = false;

        targetWord = WORDS[new Random().nextInt(WORDS.length)];

        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 5; c++) {

                Label tile = board[r][c];

                tile.setText("");

                tile.setStyle(
                        "-fx-border-color:black;" +
                                "-fx-border-width:2;" +
                                "-fx-font-size:28;" +
                                "-fx-font-weight:bold;" +
                                "-fx-text-fill:" + (themeDropdown.getValue().equals("Light") ? "black" : "white") + ";"
                );
            }
        }

        rootPane.requestFocus();
    }

    // ---------------- THEME ----------------
    private void applyTheme(String theme) {

        Scene scene = rootPane.getScene();
        if (scene == null) return;

        scene.getStylesheets().clear();

        switch (theme) {

            case "Fuschia":
                scene.getStylesheets().add(getClass().getResource("/styles/fuschia.css").toExternalForm());
                break;

            case "Willow":
                scene.getStylesheets().add(getClass().getResource("/styles/willow.css").toExternalForm());
                break;

            case "Coach":
                scene.getStylesheets().add(getClass().getResource("/styles/coach.css").toExternalForm());
                break;

            case "Ocean":
                scene.getStylesheets().add(getClass().getResource("/styles/Ocean.css").toExternalForm());
                break;

            case "Raven":
                scene.getStylesheets().add(getClass().getResource("/styles/Raven.css").toExternalForm());
                break;

            case "Bell":
                scene.getStylesheets().add(getClass().getResource("/styles/Bell.css").toExternalForm());
                break;

            case "The Four":
                scene.getStylesheets().add(getClass().getResource("/styles/Four.css").toExternalForm());
                break;

            case "Storm":
                scene.getStylesheets().add(getClass().getResource("/styles/Storm.css").toExternalForm());
                break;

            case "Orange":
                scene.getStylesheets().add(getClass().getResource("/styles/Orange.css").toExternalForm());
                break;

            case "Blue":
                scene.getStylesheets().add(getClass().getResource("/styles/blue.css").toExternalForm());
                break;

            case "Dark":
                scene.getStylesheets().add(getClass().getResource("/styles/dark.css").toExternalForm());
                break;

            case "Light":
                scene.getStylesheets().add(getClass().getResource("/styles/light.css").toExternalForm());
                break;
        }

        if (hasSubmitted) {
            forceAllTextWhite();
        }
    }

    // ---------------- FORCE TEXT WHITE AFTER SUBMIT ----------------
    private void forceAllTextWhite() {

        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 5; c++) {

                Label tile = board[r][c];

                String style = tile.getStyle();
                style = style.replaceAll("-fx-text-fill:[^;]+;", "");

                tile.setStyle(style + "-fx-text-fill:white;");
            }
        }
    }

    // ---------------- WIN ----------------
    private void showWinWindow() {

        Stage winStage = new Stage();

        Label winLabel = new Label("YOU WIN!");
        winLabel.setStyle("-fx-font-family:'Bebas Neue';-fx-font-size:40;-fx-font-weight:bold;");

        Button restartBtn = new Button("Restart");
        Button exitBtn = new Button("Exit");

        restartBtn.setOnAction(e -> {
            restartGame();
            winStage.close();
        });

        exitBtn.setOnAction(e -> System.exit(0));

        VBox layout = new VBox(15, winLabel, restartBtn, exitBtn);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 200);
        winStage.setScene(scene);
        winStage.setTitle("Victory");
        winStage.show();
    }

    // ---------------- LOSE ----------------
    private void showLoseWindow() {

        Stage loseStage = new Stage();

        Label loseLabel = new Label("YOU LOSE!");
        loseLabel.setStyle("-fx-font-family:'Bebas Neue';-fx-font-size:40;-fx-font-weight:bold;");

        Button restartBtn = new Button("Restart");
        Button exitBtn = new Button("Exit");

        restartBtn.setOnAction(e -> {
            restartGame();
            loseStage.close();
        });

        exitBtn.setOnAction(e -> System.exit(0));

        VBox layout = new VBox(15, loseLabel, restartBtn, exitBtn);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 200);
        loseStage.setScene(scene);
        loseStage.setTitle("Game Over");
        loseStage.show();
    }

    // ---------------- KEYBOARD BUTTONS ----------------
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