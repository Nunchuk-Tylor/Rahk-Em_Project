package com.elliottandcoachgeorge.javafxtest;

import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.util.*;

public class Application extends javafx.application.Application {

    private final String[] WORDS = {
            "apple","grape","chair","table","zebra","tiger","brick","plant","world","smile",
            "crane","flame","beach","sound","light","stone","train","bread","drink","crown",
            "watch","green","black","white","sweet","spice","ghost","dream","shark","money",
            "piano","robot","party","space","sugar","water","pizza","truck","mouse","happy",
            "quiet","rough","storm","touch","youth","vital","voice","radio","dance","queen"
    };

    private String targetWord;

    private final Label[][] cells = new Label[6][5];
    private final StringBuilder currentInput = new StringBuilder();

    private int currentRow = 0;

    // keyboard buttons
    private final Map<Character, Button> keyboardButtons = new HashMap<>();

    // colors
    private final String GREEN = "#6aaa64";
    private final String YELLOW = "#c9b458";
    private final String GRAY = "#787c7e";

    @Override
    public void start(Stage stage) {

        chooseWord();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");

        VBox topBox = new VBox();
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(10));

        try {
            Image image = new Image(new FileInputStream("image (4).png"));

            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(350);
            imageView.setPreserveRatio(true);

            topBox.getChildren().add(imageView);

        } catch (Exception e) {
            Label error = new Label("Could not load image.");
            topBox.getChildren().add(error);
        }

        root.setTop(topBox);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(20));

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 5; col++) {

                Label cell = new Label("");

                cell.setPrefSize(70, 70);
                cell.setAlignment(Pos.CENTER);

                cell.setFont(Font.font(28));

                cell.setStyle(
                        "-fx-border-color: black;" +
                                "-fx-border-width: 2;" +
                                "-fx-background-color: white;"
                );

                cells[row][col] = cell;

                grid.add(cell, col, row);
            }
        }

        root.setCenter(grid);

        VBox sideButtons = new VBox(15);
        sideButtons.setPadding(new Insets(20));
        sideButtons.setAlignment(Pos.CENTER);

        Button submitButton = createBigButton("SUBMIT");
        Button clearButton = createBigButton("CLEAR");
        Button restartButton = createBigButton("RESTART");
        Button exitButton = createBigButton("EXIT");

        // SUBMIT
        submitButton.setOnAction(e -> submitGuess());

        // CLEAR
        clearButton.setOnAction(e -> clearCurrentRow());

        // RESTART
        restartButton.setOnAction(e -> restartGame());

        // EXIT
        exitButton.setOnAction(e -> stage.close());

        sideButtons.getChildren().addAll(
                submitButton,
                clearButton,
                restartButton,
                exitButton
        );

        root.setRight(sideButtons);


        VBox keyboardBox = new VBox(10);
        keyboardBox.setAlignment(Pos.CENTER);
        keyboardBox.setPadding(new Insets(15));

        String[] keyboardRows = {
                "QWERTYUIOP",
                "ASDFGHJKL",
                "ZXCVBNM"
        };

        for (String row : keyboardRows) {

            HBox rowBox = new HBox(5);
            rowBox.setAlignment(Pos.CENTER);

            for (char c : row.toCharArray()) {

                Button key = new Button(String.valueOf(c));

                key.setPrefSize(45, 55);

                key.setStyle(
                        "-fx-font-size: 16;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-color: lightgray;"
                );

                keyboardButtons.put(c, key);

                key.setOnAction(e -> typeLetter(c));

                rowBox.getChildren().add(key);
            }

            keyboardBox.getChildren().add(rowBox);
        }

        root.setBottom(keyboardBox);

        Scene scene = new Scene(root, 900, 950);

        stage.setScene(scene);
        stage.setTitle("PUCKLE");
        stage.show();
    }

    private void typeLetter(char c) {

        if (currentInput.length() >= 5) return;

        currentInput.append(c);

        int col = currentInput.length() - 1;

        cells[currentRow][col].setText(String.valueOf(c));
    }

    private void clearCurrentRow() {

        currentInput.setLength(0);

        for (int i = 0; i < 5; i++) {
            cells[currentRow][i].setText("");

            cells[currentRow][i].setStyle(
                    "-fx-border-color: black;" +
                            "-fx-border-width: 2;" +
                            "-fx-background-color: white;"
            );
        }
    }

    private void submitGuess() {

        if (currentInput.length() != 5) {
            return;
        }

        String guess = currentInput.toString().toLowerCase();

        char[] target = targetWord.toCharArray();
        char[] input = guess.toCharArray();

        boolean[] used = new boolean[5];

        for (int i = 0; i < 5; i++) {

            if (input[i] == target[i]) {

                colorCell(currentRow, i, GREEN);

                keyboardButtons.get(Character.toUpperCase(input[i]))
                        .setStyle("-fx-background-color: " + GREEN + "; -fx-text-fill: white;");

                used[i] = true;
            }
        }

        for (int i = 0; i < 5; i++) {

            if (input[i] == target[i]) continue;

            boolean found = false;

            for (int j = 0; j < 5; j++) {

                if (!used[j] && input[i] == target[j]) {

                    found = true;
                    used[j] = true;
                    break;
                }
            }

            if (found) {

                colorCell(currentRow, i, YELLOW);

                keyboardButtons.get(Character.toUpperCase(input[i]))
                        .setStyle("-fx-background-color: " + YELLOW + "; -fx-text-fill: white;");

            } else {

                colorCell(currentRow, i, GRAY);

                keyboardButtons.get(Character.toUpperCase(input[i]))
                        .setStyle("-fx-background-color: " + GRAY + "; -fx-text-fill: white;");
            }
        }

        // WIN
        if (guess.equals(targetWord)) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("YOU WIN!");
            alert.setContentText("Correct word: " + targetWord.toUpperCase());
            alert.show();

            return;
        }

        currentRow++;
        currentInput.setLength(0);

        // LOSE
        if (currentRow >= 6) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("GAME OVER");
            alert.setContentText("Word was: " + targetWord.toUpperCase());
            alert.show();
        }
    }


    private void colorCell(int row, int col, String color) {

        cells[row][col].setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: 2;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;"
        );
    }

//RESTART
    private void restartGame() {

        chooseWord();

        currentRow = 0;
        currentInput.setLength(0);

        // clear board
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 5; col++) {

                cells[row][col].setText("");

                cells[row][col].setStyle(
                        "-fx-border-color: black;" +
                                "-fx-border-width: 2;" +
                                "-fx-background-color: white;"
                );
            }
        }

        // reset keyboard
        for (Button button : keyboardButtons.values()) {

            button.setStyle(
                    "-fx-font-size: 16;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-color: lightgray;"
            );
        }
    }

    // RANDOM WORD
    private void chooseWord() {

        Random random = new Random();

        targetWord = WORDS[random.nextInt(WORDS.length)];
    }

    // BUTTON STYLE

    private Button createBigButton(String text) {

        Button button = new Button(text);

        button.setPrefSize(140, 60);

        button.setStyle(
                "-fx-font-size: 18;" +
                        "-fx-font-weight: bold;"
        );

        return button;
    }
    // MAIN
    public static void main(String[] args) {
        launch(args);
    }
}