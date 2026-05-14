package com.elliottandcoachgeorge.javafxtest;

import com.elliottandcoachgeorge.javafxtest.Controllers.BabyController;
import com.elliottandcoachgeorge.javafxtest.Controllers.EasyController;
import com.elliottandcoachgeorge.javafxtest.Controllers.HardController;
import com.elliottandcoachgeorge.javafxtest.Controllers.RahkEmController;
import javafx.animation.AnimationTimer;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// This is the start screen class for my game.
// It extends Application because JavaFX apps need an Application class to start.
// I used tutorials to help with some of the JavaFX parts.
public class RahkEmStartScreen extends Application {

    // =========================================================
    // APPLICATION ENTRY POINT
    // =========================================================

    // This method runs first when the JavaFX window opens.
    // The Stage is basically the main window of the program.
    @Override
    public void start(Stage stage) {
        try {
            // FXMLLoader loads the screen layout from the FXML file.
            // FXML is used so the design and Java code are not all smashed together.
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("StartScreen.fxml")
            );

            // This connects this Java class to the FXML screen.
            // I needed this so the buttons and dropdowns would actually do things.
            loader.setController(new RahkEmStartScreen(stage));

            // root is the main layout loaded from the FXML file.
            Parent root = loader.load();

            // A Scene is what goes inside the Stage/window.
            Scene scene = new Scene(root);

            // Sets up the window title and shows the screen.
            stage.setTitle("Rahk Em'");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            // If the FXML file does not load, this prints the error.
            // This helped me debug when the file path was wrong.
            e.printStackTrace();
        }
    }

    // =========================================================
    // FXML FIELDS
    // =========================================================

    // @FXML connects things from the FXML file to this Java file.
    // These are the buttons, labels, dropdowns, and layout pieces I control with code.
    @FXML private Label titleLabel;
    @FXML private ComboBox<String> themeDropdown;
    @FXML private ComboBox<String> difficultyDropdown;
    @FXML private ComboBox<String> subjectDropdown;
    @FXML private Button freePlayButton;
    @FXML private Button dailyButton;
    @FXML private VBox rootVBox;
    @FXML private Canvas carouselCanvas;

    // This stores the current window so I can change screens later.
    private final Stage stage;

    // =========================================================
    // CAROUSEL STATE
    // =========================================================

    // These are the names of the images used in the scrolling carousel.
    // The code adds ".png" later when it loads them.
    private final String[] CAROUSEL_IMAGES = {
            "java", "geo", "lit", "science", "math", "compute", "latin"
    };

    // This list holds the actual image files after they load.
    private final List<Image> carouselImgs = new ArrayList<>();

    // AnimationTimer is what keeps the carousel moving.
    // It runs again and again, kind of like a game loop.
    private AnimationTimer carouselTimer;

    // This keeps track of how far the carousel has moved.
    private double carouselOffset = 0;

    // These control the carousel image height, spacing, and speed.
    // I made them constants so I can change the numbers in one place.
    private static final double SLOT_HEIGHT = 130;
    private static final double GAP = 30;
    private static final double SPEED = 0.7;

    // This constructor is used when I need to pass in the stage/window.
    public RahkEmStartScreen(Stage stage) {
        this.stage = stage;
    }

    // JavaFX sometimes needs a blank constructor.
    // I kept this here so the controller can still be created if needed.
    public RahkEmStartScreen() {
        this.stage = null;
    }

    // initialize runs automatically after the FXML file loads.
    // This is where I set the starting dropdown values and start animations.
    @FXML
    private void initialize() {
        // These are the default options when the start screen first opens.
        themeDropdown.setValue("WSA");
        difficultyDropdown.setValue("Medium");
        subjectDropdown.setValue("Java");

        // When the user picks a new theme, this calls applyTheme.
        // The e is the event from clicking/changing the dropdown.
        themeDropdown.setOnAction(e -> applyTheme(themeDropdown.getValue()));

        // This makes the dropdown text easier to see.
        // I had trouble with white text/colors, so this fixes it.
        styleDropdown(themeDropdown);
        styleDropdown(difficultyDropdown);
        styleDropdown(subjectDropdown);

        // Start the moving/pulsing parts of the start screen.
        startTitleAnimation();
        loadCarouselImages();
        startCarousel();

        // Apply the default theme when the screen loads.
        applyTheme("WSA");
    }

    // =========================================================
    // CAROUSEL
    // =========================================================

    // This loads all of the carousel pictures from the Images folder.
    private void loadCarouselImages() {
        for (String name : CAROUSEL_IMAGES) {
            try {
                // getResourceAsStream finds the image inside the resources folder.
                Image img = new Image(
                        getClass().getResourceAsStream("/Images/" + name + ".png")
                );

                // Add the image to the list so it can be drawn later.
                carouselImgs.add(img);

            } catch (Exception e) {
                // If one image is missing, the program does not fully crash.
                System.out.println("Could not load: " + name + ".png");
            }
        }
    }

    // This finds the width each image should be.
    // It keeps the picture from looking stretched or squished.
    private double slotWidth(int i) {
        if (i < 0 || i >= carouselImgs.size()) return SLOT_HEIGHT;

        Image img = carouselImgs.get(i);

        if (img.getHeight() == 0) return SLOT_HEIGHT;

        // This is an aspect ratio formula.
        // Width divided by height tells the code how wide it should be.
        return (img.getWidth() / img.getHeight()) * SLOT_HEIGHT;
    }

    // This calculates the total width of the whole image belt.
    // The belt means all the images plus the gaps between them.
    private double totalBeltWidth() {
        double total = 0;

        for (int i = 0; i < carouselImgs.size(); i++) {
            total += slotWidth(i) + GAP;
        }

        return total;
    }

    // This starts the actual scrolling animation.
    private void startCarousel() {
        if (carouselImgs.isEmpty()) return;

        carouselTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Move the carousel a tiny bit every frame.
                carouselOffset += SPEED;

                double belt = totalBeltWidth();

                // If the carousel reaches the end, start it over.
                // This makes it look like it loops forever.
                if (carouselOffset >= belt) {
                    carouselOffset -= belt;
                }

                // Redraw the carousel in the new position.
                drawCarousel();
            }
        };

        carouselTimer.start();
    }

    // This draws the carousel images onto the canvas.
    private void drawCarousel() {
        if (carouselCanvas == null || carouselImgs.isEmpty()) return;

        // GraphicsContext is the tool used to draw on a Canvas.
        GraphicsContext gc = carouselCanvas.getGraphicsContext2D();

        double canvasW = carouselCanvas.getWidth();
        double canvasH = carouselCanvas.getHeight();
        double belt = totalBeltWidth();

        // Clear the old frame before drawing the new one.
        gc.clearRect(0, 0, canvasW, canvasH);

        // I draw three sets of the same images so there are no empty gaps.
        for (int cycle = 0; cycle < 3; cycle++) {
            double x = cycle * belt - carouselOffset;

            for (int i = 0; i < carouselImgs.size(); i++) {
                double w = slotWidth(i);
                double y = (canvasH - SLOT_HEIGHT) / 2.0;

                // Only draw the image if it is actually on the screen.
                // This is a small performance thing.
                if (x + w >= 0 && x <= canvasW) {
                    gc.drawImage(carouselImgs.get(i), x, y, w, SLOT_HEIGHT);
                }

                // Move x over for the next image.
                x += w + GAP;
            }
        }
    }

    // =========================================================
    // DROPDOWN WHITE TEXT FIX
    // =========================================================

    // This method changes how the dropdown text looks.
    // I had an issue where some dropdown text was hard to read.
    private void styleDropdown(ComboBox<String> dropdown) {
        // This styles the selected item that shows before you open the dropdown.
        dropdown.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
                }
            }
        });

        // This styles the list of choices after the dropdown is opened.
        dropdown.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white; -fx-background-color: #2f2f2f;");
                }
            }
        });
    }

    // =========================================================
    // TITLE ANIMATION
    // =========================================================

    // This makes the title grow and shrink over and over.
    private void startTitleAnimation() {
        // ScaleTransition changes the size of something over time.
        ScaleTransition grow = new ScaleTransition(Duration.millis(800), titleLabel);
        grow.setFromX(1.0);
        grow.setFromY(1.0);
        grow.setToX(1.15);
        grow.setToY(1.15);

        ScaleTransition shrink = new ScaleTransition(Duration.millis(800), titleLabel);
        shrink.setFromX(1.15);
        shrink.setFromY(1.15);
        shrink.setToX(1.0);
        shrink.setToY(1.0);

        // When grow finishes, shrink starts.
        grow.setOnFinished(e -> shrink.play());

        // When shrink finishes, grow starts again.
        shrink.setOnFinished(e -> grow.play());

        // Starts the animation.
        grow.play();
    }

    // =========================================================
    // THEME ENGINE
    // =========================================================

    // This changes the colors and styles when a theme is selected.
    private void applyTheme(String theme) {
        // These methods return the correct colors for the selected theme.
        String bg = getThemeBg(theme);
        String btnBg = getThemeBtnBg(theme);
        String btnText = getThemeBtnText(theme);
        String titleText = getThemeTitleText(theme);
        String dropBg = getThemeDropBg(theme);

        // Change the background color.
        rootVBox.setStyle("-fx-background-color:" + bg + ";");

        // Style the title label.
        titleLabel.setStyle(
                "-fx-font-size:64;" +
                        "-fx-font-weight:bold;" +
                        "-fx-text-fill:" + titleText + ";"
        );

        // This creates one button style and uses it for both buttons.
        String btnStyle =
                "-fx-font-size:18;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-color:" + btnBg + ";" +
                        "-fx-text-fill:" + btnText + ";" +
                        "-fx-background-radius:10;";

        freePlayButton.setStyle(btnStyle);
        dailyButton.setStyle(btnStyle);

        // This styles the dropdown boxes.
        String dropStyle =
                "-fx-background-color:" + dropBg + ";" +
                        "-fx-background-radius:8;" +
                        "-fx-text-fill:white;";

        themeDropdown.setStyle(dropStyle);
        difficultyDropdown.setStyle(dropStyle);
        subjectDropdown.setStyle(dropStyle);

        final String finalDropBg = dropBg;

        // This loops through all three dropdowns and styles them.
        // This keeps me from writing the same code three times.
        for (ComboBox<String> dropdown : new ComboBox[]{themeDropdown, difficultyDropdown, subjectDropdown}) {
            dropdown.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
                    }
                }
            });

            dropdown.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle("-fx-text-fill: white; -fx-background-color:" + finalDropBg + ";");
                    }
                }
            });
        }
    }

    // =========================================================
    // THEME COLOR MAPS
    // =========================================================

    // These methods are basically color lookup tables.
    // I used switch statements because there are a lot of themes.

    private String getThemeBg(String theme) {
        switch (theme) {
            case "WSA":      return "white";
            case "Dark":     return "#1a1a1a";
            case "Light":    return "#f5f5f5";
            case "Blue":     return "#1a2a4a";
            case "Ocean":    return "#003d56";
            case "Raven":    return "#1e1e30";
            case "Coach":    return "#90D5FF";
            case "DMac":     return "#850101";
            case "Willow":   return "#1e3318";
            case "Fuschia":  return "#3b0a3b";
            case "Joiner":   return "#153204";
            case "Bell":     return "#FFD0E3";
            case "The Four": return "#41A2F5";
            case "Storm":    return "#090913";
            case "Orange":   return "#3b2000";
            default:         return "#1a1a1a";
        }
    }

    private String getThemeBtnBg(String theme) {
        switch (theme) {
            case "WSA":      return "#0a1a5c";
            case "Dark":     return "#2f2f2f";
            case "Light":    return "#d3d6da";
            case "Blue":     return "#64C4FF";
            case "Ocean":    return "#006994";
            case "Raven":    return "#3b3b58";
            case "Coach":    return "#90D5FF";
            case "DMac":     return "#B31942";
            case "Willow":   return "#4f7942";
            case "Fuschia":  return "#c154c1";
            case "Joiner":   return "#001e60";
            case "Bell":     return "#FFA6C9";
            case "The Four": return "#6a5acd";
            case "Storm":    return "#1E1E44";
            case "Orange":   return "#ff8c00";
            default:         return "#2f2f2f";
        }
    }

    private String getThemeBtnText(String theme) {
        switch (theme) {
            case "Light": return "#222222";
            case "Bell":  return "#222222";
            default:      return "white";
        }
    }

    private String getThemeTitleText(String theme) {
        switch (theme) {
            case "WSA":     return "#0a1a5c";
            case "Light":   return "#222222";
            case "Bell":    return "black";
            case "DMac":    return "#FFFFFF";
            case "Joiner":  return "#001e60";
            case "Math":    return "#1a237e";
            case "Compute": return "#0d1b6e";
            case "Geo":     return "#1a3a6e";
            case "Science": return "#0d2b5e";
            case "Lit":     return "#1a1f6e";
            default:        return "white";
        }
    }

    private String getThemeDropBg(String theme) {
        switch (theme) {
            case "WSA":      return "#0a1a5c";
            case "Light":    return "#d3d6da";
            case "Blue":     return "#4d79ff";
            case "Ocean":    return "#006994";
            case "Raven":    return "#3b3b58";
            case "Coach":    return "#90D5FF";
            case "DMac":     return "#0A3161";
            case "Joiner":   return "#153204";
            case "Willow":   return "#4f7942";
            case "Fuschia":  return "#c154c1";
            case "Bell":     return "#FFA6C9";
            case "The Four": return "#6a5acd";
            case "Storm":    return "#1E1E44";
            case "Orange":   return "#ff8c00";
            default:         return "#2f2f2f";
        }
    }

    // =========================================================
    // BUTTON ACTIONS
    // =========================================================

    // This runs when the Free Play button is clicked.
    @FXML
    private void startFreePlay() {
        String difficulty = difficultyDropdown.getValue();
        String subject = subjectDropdown.getValue();

        // If nothing is picked, use default values so the game does not break.
        if (difficulty == null) difficulty = "Medium";
        if (subject == null) subject = "Java";

        // Start the game in free play mode.
        loadGame(GameMode.FREE_PLAY, difficulty, subject);
    }

    // This runs when the Daily Challenge button is clicked.
    @FXML
    private void startDailyChallenge() {
        String difficulty = difficultyDropdown.getValue();
        String subject = subjectDropdown.getValue();

        // Backup/default values.
        if (difficulty == null) difficulty = "Medium";
        if (subject == null) subject = "Java";

        // Start the game in daily challenge mode.
        loadGame(GameMode.DAILY, difficulty, subject);
    }

    // =========================================================
    // GAME LOADER
    // =========================================================

    // This decides which version of the game to load.
    private void loadGame(GameMode mode, String difficulty, String subject) {
        try {
            // Stop the carousel before switching screens so it does not keep running.
            if (carouselTimer != null) carouselTimer.stop();

            String theme = themeDropdown.getValue();

            // Load a different screen depending on the difficulty.
            switch (difficulty) {
                case "Baby":   loadBabyGame(mode, theme, subject);   break;
                case "Easy":   loadEasyGame(mode, theme, subject);   break;
                case "Hard":   loadHardGame(mode, theme, subject);   break;
                case "Medium":
                default:       loadMediumGame(mode, theme, subject); break;
            }

        } catch (Exception e) {
            // Prints the error if something goes wrong while loading the game.
            e.printStackTrace();
        }
    }

    // Loads the medium version of the game.
    private void loadMediumGame(GameMode mode, String theme, String subject) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
        Parent root = loader.load();

        // Get the controller so this start screen can send information to the game screen.
        RahkEmController controller = loader.getController();

        // This lets the game screen send the player back to the start screen.
        controller.setBackCallback(this::showStartScreen);

        // Passes theme, subject, and game mode to the game controller.
        controller.applyThemeExternal(theme);
        controller.setSubject(subject);
        controller.setGameMode(mode);

        Scene scene = new Scene(root);
        applyThemeToScene(scene, theme);

        stage.setScene(scene);
        stage.setTitle("Rahk Em' - Medium");
        stage.show();
    }

    // Loads the baby difficulty version.
    private void loadBabyGame(GameMode mode, String theme, String subject) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("baby-view.fxml"));
        Parent root = loader.load();

        BabyController controller = loader.getController();
        controller.setBackCallback(this::showStartScreen);
        controller.applyThemeExternal(theme);
        controller.setSubject(subject);
        controller.setGameMode(mode);

        Scene scene = new Scene(root);
        applyThemeToScene(scene, theme);

        stage.setScene(scene);
        stage.setTitle("Rahk Em' - Baby");
        stage.show();
    }

    // Loads the easy difficulty version.
    private void loadEasyGame(GameMode mode, String theme, String subject) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("easy-view.fxml"));
        Parent root = loader.load();

        EasyController controller = loader.getController();
        controller.setBackCallback(this::showStartScreen);
        controller.applyThemeExternal(theme);
        controller.setSubject(subject);
        controller.setGameMode(mode);

        Scene scene = new Scene(root);
        applyThemeToScene(scene, theme);

        stage.setScene(scene);
        stage.setTitle("Rahk Em' - Easy");
        stage.show();
    }

    // Loads the hard difficulty version.
    private void loadHardGame(GameMode mode, String theme, String subject) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hard-view.fxml"));
        Parent root = loader.load();

        HardController controller = loader.getController();
        controller.setBackCallback(this::showStartScreen);
        controller.applyThemeExternal(theme);
        controller.setSubject(subject);
        controller.setGameMode(mode);

        Scene scene = new Scene(root);
        applyThemeToScene(scene, theme);

        stage.setScene(scene);
        stage.setTitle("Rahk Em' - Hard");
        stage.show();
    }

    // =========================================================
    // THEME HELPERS
    // =========================================================

    // This adds the correct CSS file to the scene.
    // CSS is what controls a lot of the visual styling.
    private void applyThemeToScene(Scene scene, String theme) {
        // Clear old styles first so they do not stack on top of each other.
        scene.getStylesheets().clear();

        String cssPath = getCssPath(theme);

        if (cssPath != null) {
            try {
                scene.getStylesheets().add(
                        getClass().getResource(cssPath).toExternalForm()
                );
            } catch (Exception ignored) {
                // If the stylesheet does not load, the game still keeps going.
            }
        }
    }

    // This returns the CSS file path for each theme.
    private String getCssPath(String theme) {
        switch (theme) {
            case "WSA":      return "/styles/wsa.css";
            case "Dark":     return "/styles/dark.css";
            case "Light":    return "/styles/light.css";
            case "Blue":     return "/styles/blue.css";
            case "Ocean":    return "/styles/Ocean.css";
            case "Raven":    return "/styles/Raven.css";
            case "Coach":    return "/styles/coach.css";
            case "Willow":   return "/styles/willow.css";
            case "Fuschia":  return "/styles/fuschia.css";
            case "Bell":     return "/styles/Bell.css";
            case "DMac":     return "/styles/DMac.css";
            case "Joiner":   return "/styles/Joiner.css";
            case "The Four": return "/styles/Four.css";
            case "Storm":    return "/styles/Storm.css";
            case "Orange":   return "/styles/Orange.css";
            default:         return "/styles/wsa.css";
        }
    }

    // =========================================================
    // RETURN FROM GAME
    // =========================================================

    // This reloads the start screen when the player comes back from the game.
    public void showStartScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("StartScreen.fxml"));

            // Give the new start screen access to the same stage/window.
            loader.setController(new RahkEmStartScreen(stage));

            Parent root = loader.load();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("Rahk Em'");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}