package com.musicplayer.ui.javafx;

import com.musicplayer.application.controller.PlayerController;
import com.musicplayer.domain.model.Library;
import com.musicplayer.domain.model.PlaybackState;
import com.musicplayer.domain.model.RepeatMode;
import com.musicplayer.domain.model.Track;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public class MusicPlayerUI extends BorderPane {
    
    private static final Logger logger = LoggerFactory.getLogger(MusicPlayerUI.class);
    
    private static final Color PRIMARY_COLOR = Color.rgb(102, 126, 234);
    private static final Color ACCENT_COLOR = Color.rgb(118, 75, 162);
    
    private final PlayerController controller;
    
    private QueuePanel queuePanel;
    private BrowserPanel browserPanel;
    private ArtworkPanel artworkPanel;
    
    private Label titleLabel;
    private Label artistLabel;
    private Slider progressSlider;
    private Label currentTimeLabel;
    private Label totalTimeLabel;
    private Slider volumeSlider;
    private Button playPauseButton;
    private Button previousButton;
    private Button nextButton;
    private Button shuffleButton;
    private Button repeatButton;
    private Label statusLabel;

    public MusicPlayerUI(PlayerController controller) {
        this.controller = controller;
        setupBackground();
        setupUI();
        setupEventHandlers();
        setupDragAndDrop();
        
        controller.initializeLibrary();
    }
    
    private void setupBackground() {
        setStyle("-fx-background-color: #f8f9fc;");
        
        BackgroundFill backgroundFill = new BackgroundFill(
            new LinearGradient(0, 0, 0, 1, true, null,
                new Stop(0, Color.rgb(248, 249, 252)),
                new Stop(1, Color.rgb(233, 236, 244))
            ),
            CornerRadii.EMPTY, Insets.EMPTY
        );
        setBackground(new Background(backgroundFill));
    }

    private void setupUI() {
        MenuBar menuBar = createMenuBar();
        setTop(menuBar);
        
        queuePanel = new QueuePanel();
        queuePanel.setOnTrackDoubleClicked((track, index) -> controller.playTrack(index));
        setLeft(queuePanel);
        
        browserPanel = new BrowserPanel();
        browserPanel.setOnCategorySelected((category, item, tracks) -> browserPanel.showNowPlaying(item.name(), tracks));
        browserPanel.setOnStartPlaylist(this::startPlaylist);
        
        StackPane centerContainer = new StackPane(browserPanel);
        centerContainer.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-width: 0;"
        );
        setCenter(centerContainer);
        
        HBox bottomContent = new HBox();
        bottomContent.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-width: 1 0 0 0;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-padding: 16 24;"
        );
        
        VBox controlsSection = new VBox(10);
        controlsSection.setAlignment(Pos.CENTER);
        HBox.setHgrow(controlsSection, Priority.ALWAYS);
        
        VBox trackInfoBox = createTrackInfoPanel();
        HBox progressBox = createProgressPanel();
        createControlsPanel();
        HBox volumeBox = createVolumePanel();
        
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        statusLabel.setAlignment(Pos.CENTER);
        
        HBox controlsRow = new HBox(12);
        controlsRow.setAlignment(Pos.CENTER);
        controlsRow.getChildren().addAll(shuffleButton, previousButton, playPauseButton, 
                nextButton, repeatButton);
        
        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.CENTER);
        bottomRow.getChildren().addAll(volumeBox, new Region());
        
        controlsSection.getChildren().addAll(trackInfoBox, progressBox, controlsRow, bottomRow, statusLabel);
        
        artworkPanel = new ArtworkPanel(120);
        StackPane artworkContainer = createGlassPane(artworkPanel, 130, 130);
        
        bottomContent.getChildren().addAll(controlsSection, artworkContainer);
        setBottom(bottomContent);
    }
    
    private StackPane createGlassPane(Node content, double width, double height) {
        StackPane pane = new StackPane(content);
        pane.setMinSize(width, height);
        pane.setMaxSize(width, height);
        pane.setPrefSize(width, height);
        pane.setStyle(
            "-fx-background-color: #f1f5f9;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-padding: 8;"
        );
        pane.setEffect(new DropShadow(8, 0, 4, Color.rgb(0, 0, 0, 0.08)));
        return pane;
    }
    
    private MenuBar createMenuBar() {
        Menu fileMenu = new Menu("File");
        fileMenu.setStyle("-fx-text-fill: #334155;");
        
        MenuItem openDir = new MenuItem("Open Directory");
        openDir.setOnAction(e -> openDirectory());
        openDir.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+O"));
        styleMenuItem(openDir);
        
        MenuItem rescan = new MenuItem("Rescan Library");
        rescan.setOnAction(e -> rescanLibrary());
        styleMenuItem(rescan);
        
        MenuItem fullRescan = new MenuItem("Full Rescan");
        fullRescan.setOnAction(e -> forceFullRescan());
        styleMenuItem(fullRescan);
        
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> {
            controller.dispose();
            Platform.exit();
        });
        styleMenuItem(exit);
        
        fileMenu.getItems().addAll(openDir, new SeparatorMenuItem(), rescan, fullRescan, 
                new SeparatorMenuItem(), exit);
        
        Menu helpMenu = new Menu("Help");
        helpMenu.setStyle("-fx-text-fill: #334155;");
        
        MenuItem about = new MenuItem("About");
        about.setOnAction(e -> showAbout());
        styleMenuItem(about);
        
        helpMenu.getItems().add(about);
        
        MenuBar menuBar = new MenuBar(fileMenu, helpMenu);
        menuBar.setStyle(
            "-fx-background-color: white;" +
            "-fx-padding: 8 16;" +
            "-fx-border-width: 0 0 1 0;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-font-size: 13px;"
        );
        return menuBar;
    }
    
    private void styleMenuItem(MenuItem item) {
        item.setStyle("-fx-text-fill: #334155; -fx-background-color: transparent;");
    }
    
    private VBox createTrackInfoPanel() {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        
        titleLabel = new Label("No track loaded");
        titleLabel.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1e293b;" +
            "-fx-font-smoothing-type: lcd;"
        );
        
        artistLabel = new Label("");
        artistLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #64748b;"
        );
        
        box.getChildren().addAll(titleLabel, artistLabel);
        return box;
    }
    
    private HBox createProgressPanel() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER);
        
        currentTimeLabel = new Label("0:00");
        currentTimeLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-font-family: 'SF Mono', 'Menlo', monospace;" +
            "-fx-text-fill: #94a3b8;" +
            "-fx-min-width: 45;" +
            "-fx-alignment: center;"
        );
        
        progressSlider = new Slider(0, 100, 0);
        progressSlider.setPrefWidth(350);
        progressSlider.setBlockIncrement(1);
        styleSlider(progressSlider);
        
        totalTimeLabel = new Label("0:00");
        totalTimeLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-font-family: 'SF Mono', 'Menlo', monospace;" +
            "-fx-text-fill: #94a3b8;" +
            "-fx-min-width: 45;" +
            "-fx-alignment: center;"
        );
        
        box.getChildren().addAll(currentTimeLabel, progressSlider, totalTimeLabel);
        return box;
    }
    
    private void styleSlider(Slider slider) {
        slider.setStyle(
            "-fx-control-inner-background: #e2e8f0;" +
            "-fx-thumb-color: " + toRgbString(PRIMARY_COLOR) + ";" +
            "-fx-range-bar: linear-gradient(90deg, #667eea 0%, #764ba2 100%);"
        );
    }
    
    private String toRgbString(Color color) {
        return String.format("rgb(%d, %d, %d)", 
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }
    
    private void createControlsPanel() {
        previousButton = createGlassButton(IconFactory.createPreviousIcon(), 36, 36);
        previousButton.setOnAction(e -> controller.previous());
        
        playPauseButton = createAccentButton(IconFactory.createPlayIcon(), 52, 52);
        playPauseButton.setOnAction(e -> controller.togglePlayPause());
        
        nextButton = createGlassButton(IconFactory.createNextIcon(), 36, 36);
        nextButton.setOnAction(e -> controller.next());
        
        shuffleButton = createGlassButton(IconFactory.createShuffleIcon(), 32, 32);
        shuffleButton.setOnAction(e -> {
            controller.toggleShuffle();
            updateShuffleButton();
        });
        
        repeatButton = createGlassButton(IconFactory.createRepeatIcon(), 32, 32);
        repeatButton.setOnAction(e -> {
            controller.toggleRepeat();
            updateRepeatButton();
        });
    }
    
    private Button createGlassButton(Node icon, double size, double iconSize) {
        Button button = new Button();
        button.setGraphic(icon);
        button.setMinSize(size, size);
        button.setPrefSize(size, size);
        button.setMaxSize(size, size);
        
        scaleIcon(icon, iconSize);
        
        button.setStyle(
            "-fx-background-color: #f1f5f9;" +
            "-fx-background-radius: 50;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 50;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 0;"
        );
        
        addButtonHoverEffect(button);
        return button;
    }
    
    private Button createAccentButton(Node icon, double size, double iconSize) {
        Button button = new Button();
        button.setGraphic(icon);
        button.setMinSize(size, size);
        button.setPrefSize(size, size);
        button.setMaxSize(size, size);
        
        scaleIcon(icon, iconSize);
        
        button.setStyle(
            "-fx-background-radius: 50;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 0;" +
            "-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);" +
            "-fx-border-radius: 50;" +
            "-fx-border-width: 0;" +
            "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.3), 10, 0.2, 0, 3);"
        );
        
        addButtonHoverEffect(button);
        return button;
    }
    
    private void scaleIcon(Node icon, double size) {
        if (icon instanceof javafx.scene.Group group) {
            group.setScaleX(size / 24);
            group.setScaleY(size / 24);
        } else if (icon instanceof javafx.scene.shape.SVGPath svg) {
            svg.setScaleX(size / 24);
            svg.setScaleY(size / 24);
        }
    }
    
    private void addButtonHoverEffect(Button button) {
        button.setOnMouseEntered(e -> {
            String baseStyle = button.getStyle();
            if (baseStyle.contains("gradient")) {
                button.setStyle(baseStyle + "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.5), 15, 0.3, 0, 4);");
            } else {
                button.setStyle(baseStyle.replace("#f1f5f9", "#e2e8f0"));
            }
        });
        button.setOnMouseExited(e -> {
            String baseStyle = button.getStyle();
            if (baseStyle.contains("gradient")) {
                button.setStyle(baseStyle.replaceAll("-fx-effect: dropshadow\\(gaussian, rgba\\([^)]+\\)\\);?", ""));
            } else {
                button.setStyle(baseStyle.replace("#e2e8f0", "#f1f5f9"));
            }
        });
    }
    
    private HBox createVolumePanel() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Node volumeIcon = IconFactory.createVolumeIcon();
        setIconColor((javafx.scene.shape.SVGPath)volumeIcon, Color.rgb(100, 116, 139));
        ((javafx.scene.shape.SVGPath)volumeIcon).setScaleX(0.85);
        ((javafx.scene.shape.SVGPath)volumeIcon).setScaleY(0.85);
        
        volumeSlider = new Slider(0, 100, 70);
        volumeSlider.setPrefWidth(100);
        styleSlider(volumeSlider);
        
        box.getChildren().addAll(volumeIcon, volumeSlider);
        return box;
    }
    
    private void setIconColor(javafx.scene.shape.SVGPath path, Color color) {
        path.setFill(color);
    }

    private void setupEventHandlers() {
        playPauseButton.setOnAction(e -> controller.togglePlayPause());
        previousButton.setOnAction(e -> controller.previous());
        nextButton.setOnAction(e -> controller.next());
        shuffleButton.setOnAction(e -> {
            controller.toggleShuffle();
            updateShuffleButton();
        });
        repeatButton.setOnAction(e -> {
            controller.toggleRepeat();
            updateRepeatButton();
        });
        
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            controller.setVolume(newVal.doubleValue() / 100.0);
        });
        
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (progressSlider.isValueChanging()) {
                controller.seekToPercentage(newVal.doubleValue() / 100.0);
            }
        });
        
        controller.addStateListener(new PlayerController.PlayerStateListener() {
            @Override
            public void onPlaybackStateChanged(PlaybackState state) {
                Platform.runLater(() -> updatePlaybackState(state));
            }

            @Override
            public void onTrackChanged(Track track) {
                Platform.runLater(() -> {
                    updateTrackInfo(track);
                    queuePanel.setCurrentTrackIndex(controller.getCurrentIndex());
                });
            }

            @Override
            public void onProgressChanged(Duration position, Duration duration) {
                Platform.runLater(() -> updateProgress(position, duration));
            }

            @Override
            public void onVolumeChanged(double volume) {
                Platform.runLater(() -> volumeSlider.setValue(volume * 100));
            }

            @Override
            public void onPlaylistChanged(List<Track> playlist) {
                Platform.runLater(() -> queuePanel.setTracks(playlist));
            }

            @Override
            public void onPlaybackModeChanged(RepeatMode repeatMode, boolean shuffled) {
                Platform.runLater(() -> {
                    updateRepeatButton();
                    updateShuffleButton();
                });
            }

            @Override
            public void onError(String message) {
                Platform.runLater(() -> showError(message));
            }

            @Override
            public void onInfo(String message) {
                Platform.runLater(() -> statusLabel.setText(message));
            }

            @Override
            public void onLibraryLoaded(Library library) {
                Platform.runLater(() -> {
                    browserPanel.setLibrary(library);
                    List<Track> tracks = convertLibraryToTracks(library);
                    queuePanel.setTracks(tracks);
                });
            }

            @Override
            public void onLibraryChanged(Library library) {
                Platform.runLater(() -> {
                    browserPanel.setLibrary(library);
                    List<Track> tracks = convertLibraryToTracks(library);
                    queuePanel.setTracks(tracks);
                });
            }
        });
        
        volumeSlider.setValue(controller.getVolume() * 100);
    }
    
    private List<Track> convertLibraryToTracks(Library library) {
        if (library == null) return List.of();
        return library.getEntries().stream()
                .map(entry -> new Track(
                        entry.path().toString(),
                        entry.title(),
                        entry.artist(),
                        entry.album(),
                        entry.duration()
                ))
                .toList();
    }

    private void setupDragAndDrop() {
        setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        
        setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                Path dir = db.getFiles().get(0).toPath();
                if (dir.toFile().isDirectory()) {
                    controller.addImportPath(dir, null);
                }
            }
            event.consume();
        });
        
        setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case SPACE -> controller.togglePlayPause();
                case LEFT -> controller.previous();
                case RIGHT -> controller.next();
                case UP -> controller.setVolume(controller.getVolume() + 0.05);
                case DOWN -> controller.setVolume(controller.getVolume() - 0.05);
                case M -> controller.setVolume(0);
            }
        });
    }

    private void openDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Music Directory");
        
        Stage stage = (Stage) getScene().getWindow();
        java.io.File result = chooser.showDialog(stage);
        
        if (result != null) {
            controller.addImportPath(result.toPath(), null);
        }
    }
    
    private void rescanLibrary() {
        statusLabel.setText("Scanning for changes...");
        controller.rescanLibrary(progress -> {
            logger.debug("Scan progress: {}/{} - {}", progress.current(), progress.total(), progress.currentFile());
        });
    }
    
    private void forceFullRescan() {
        statusLabel.setText("Full rescan in progress...");
        controller.forceFullRescan(progress -> {
            logger.debug("Full rescan: {}", progress.currentFile());
        });
    }

    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Music Player");
        alert.setHeaderText("Music Player v2.0");
        alert.setContentText("A modern music player for MP3 files.\n\nBuilt with Java and JavaFX.");
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );
        dialogPane.lookup(".header-panel").setStyle("-fx-background-color: transparent;");
        alert.showAndWait();
    }

    private void updatePlaybackState(PlaybackState state) {
        Node icon;
        switch (state) {
            case PLAYING -> {
                icon = IconFactory.createPauseIcon();
                statusLabel.setText("Playing");
            }
            case PAUSED -> {
                icon = IconFactory.createPlayIcon();
                statusLabel.setText("Paused");
            }
            case STOPPED, IDLE -> {
                icon = IconFactory.createPlayIcon();
                statusLabel.setText("Stopped");
            }
            default -> {
                icon = IconFactory.createPlayIcon();
                statusLabel.setText("");
            }
        }
        playPauseButton.setGraphic(icon);
        scaleIcon(icon, 52);
    }

    private void updateTrackInfo(Track track) {
        if (track != null) {
            titleLabel.setText(track.getTitle());
            artistLabel.setText(track.getArtist());
            if (artworkPanel != null) {
                if (track.hasArtwork()) {
                    artworkPanel.setArtwork(track.getArtwork());
                } else {
                    artworkPanel.setPlaceholder();
                }
            }
        } else {
            titleLabel.setText("No track loaded");
            artistLabel.setText("");
            if (artworkPanel != null) {
                artworkPanel.clear();
            }
        }
    }
    
    private void startPlaylist(List<Track> tracks) {
        if (!tracks.isEmpty()) {
            controller.setPlaylist(tracks);
            queuePanel.setTracks(tracks);
            controller.playTrack(0);
            statusLabel.setText("Playing: " + tracks.size() + " tracks");
        }
    }

    private void updateProgress(Duration position, Duration duration) {
        currentTimeLabel.setText(formatDuration(position));
        totalTimeLabel.setText(formatDuration(duration));
        
        if (duration != null && !duration.isZero()) {
            double percentage = (double) position.toMillis() / duration.toMillis() * 100;
            if (!progressSlider.isValueChanging()) {
                progressSlider.setValue(percentage);
            }
        }
    }

    private void updateRepeatButton() {
        RepeatMode mode = controller.getRepeatMode();
        Node icon = switch (mode) {
            case OFF -> IconFactory.createRepeatIcon();
            case ALL -> IconFactory.createRepeatIcon();
            case ONE -> IconFactory.createRepeatOneIcon();
            default -> IconFactory.createRepeatIcon();
        };
        repeatButton.setGraphic(icon);
        setIconColor((javafx.scene.shape.SVGPath)icon, Color.rgb(100, 116, 139));
        scaleIcon(icon, 32);
    }

    private void updateShuffleButton() {
        boolean shuffled = controller.isShuffled();
        shuffleButton.setOpacity(shuffled ? 1.0 : 0.5);
    }

    private void showError(String message) {
        statusLabel.setText("Error: " + message);
        statusLabel.setStyle("-fx-text-fill: #ef4444;");
        
        PauseTransition pause = new PauseTransition(javafx.util.Duration.millis(5000));
        pause.setOnFinished(event -> statusLabel.setStyle("-fx-text-fill: #94a3b8;"));
        pause.play();
    }

    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "0:00";
        }
        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
