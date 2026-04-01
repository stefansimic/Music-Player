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
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main JavaFX UI for the Music Player application (v2.0).
 * 
 * Uses a BorderPane layout with:
 * - Left: Collapsible QueuePanel
 * - Center: BrowserPanel for Albums/Artists/Playlists/Genres
 * - Bottom: ControlsPanel with playback controls
 * - Bottom-Right: ArtworkPanel for album art
 */
public class MusicPlayerUI extends BorderPane {
    
    private static final Logger logger = LoggerFactory.getLogger(MusicPlayerUI.class);
    
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
        setupUI();
        setupEventHandlers();
        setupDragAndDrop();
        
        controller.initializeLibrary();
    }

    private void setupUI() {
        setStyle("-fx-background-color: #f5f5f5;");
        
        MenuBar menuBar = createMenuBar();
        setTop(menuBar);
        
        queuePanel = new QueuePanel();
        queuePanel.setOnTrackDoubleClicked((track, index) -> controller.playTrack(index));
        setLeft(queuePanel);
        
        browserPanel = new BrowserPanel();
        browserPanel.setOnCategorySelected((category, item, tracks) -> browserPanel.showNowPlaying(item.name(), tracks));
        browserPanel.setOnStartPlaylist(this::startPlaylist);
        
        setCenter(browserPanel);
        
        VBox bottomPanel = createBottomPanel();
        setBottom(bottomPanel);
        
        artworkPanel = new ArtworkPanel(120);
        StackPane artworkContainer = new StackPane(artworkPanel);
        artworkContainer.setPadding(new Insets(0, 10, 10, 0));
        setRight(artworkContainer);
    }
    
    private MenuBar createMenuBar() {
        Menu fileMenu = new Menu("File");
        
        MenuItem openDir = new MenuItem("Open Directory");
        openDir.setOnAction(e -> openDirectory());
        openDir.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+O"));
        
        MenuItem rescan = new MenuItem("Rescan Library");
        rescan.setOnAction(e -> rescanLibrary());
        
        MenuItem fullRescan = new MenuItem("Full Rescan");
        fullRescan.setOnAction(e -> forceFullRescan());
        
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> {
            controller.dispose();
            Platform.exit();
        });
        
        fileMenu.getItems().addAll(openDir, new SeparatorMenuItem(), rescan, fullRescan, 
                new SeparatorMenuItem(), exit);
        
        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About");
        about.setOnAction(e -> showAbout());
        helpMenu.getItems().add(about);
        
        return new MenuBar(fileMenu, helpMenu);
    }
    
    private VBox createBottomPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setBackground(new Background(new BackgroundFill(
                javafx.scene.paint.Color.rgb(220, 220, 220), 
                new CornerRadii(0), Insets.EMPTY)));
        
        VBox trackInfoBox = createTrackInfoPanel();
        
        HBox progressBox = createProgressPanel();
        
        createControlsPanel();
        
        HBox volumeBox = createVolumePanel();
        
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #888888;");
        
        HBox controlsRow = new HBox(20);
        controlsRow.setAlignment(Pos.CENTER);
        controlsRow.getChildren().addAll(shuffleButton, previousButton, playPauseButton, 
                nextButton, repeatButton);
        
        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.CENTER);
        bottomRow.getChildren().addAll(volumeBox, new Region());
        
        panel.getChildren().addAll(trackInfoBox, progressBox, controlsRow, bottomRow, statusLabel);
        
        return panel;
    }
    
    private VBox createTrackInfoPanel() {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        
        titleLabel = new Label("No track loaded");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        artistLabel = new Label("");
        artistLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
        
        box.getChildren().addAll(titleLabel, artistLabel);
        return box;
    }
    
    private HBox createProgressPanel() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        
        currentTimeLabel = new Label("0:00");
        currentTimeLabel.setPrefWidth(50);
        
        progressSlider = new Slider(0, 100, 0);
        progressSlider.setPrefWidth(400);
        progressSlider.setBlockIncrement(1);
        
        totalTimeLabel = new Label("0:00");
        totalTimeLabel.setPrefWidth(50);
        
        box.getChildren().addAll(currentTimeLabel, progressSlider, totalTimeLabel);
        return box;
    }
    
    private void createControlsPanel() {
        previousButton = new Button("⏮");
        previousButton.setStyle("-fx-font-size: 20px; -fx-background-color: transparent;");
        
        playPauseButton = new Button("▶");
        playPauseButton.setStyle("-fx-font-size: 30px; -fx-background-color: transparent;");
        
        nextButton = new Button("⏭");
        nextButton.setStyle("-fx-font-size: 20px; -fx-background-color: transparent;");
        
        shuffleButton = new Button("🔀");
        shuffleButton.setStyle("-fx-font-size: 16px; -fx-background-color: transparent;");
        
        repeatButton = new Button("🔁");
        repeatButton.setStyle("-fx-font-size: 16px; -fx-background-color: transparent;");
    }
    
    private HBox createVolumePanel() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        
        Label volumeIcon = new Label("🔊");
        
        volumeSlider = new Slider(0, 100, 70);
        volumeSlider.setPrefWidth(150);
        
        box.getChildren().addAll(volumeIcon, volumeSlider);
        return box;
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
                Platform.runLater(() -> {
                    queuePanel.setTracks(playlist);
                });
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
        alert.setHeaderText("Music Player v2.0.0");
        alert.setContentText("A music player for MP3 files with library management.\n\nBuilt with Java and JavaFX.");
        alert.showAndWait();
    }

    private void updatePlaybackState(PlaybackState state) {
        switch (state) {
            case PLAYING -> {
                playPauseButton.setText("⏸");
                statusLabel.setText("Playing");
            }
            case PAUSED -> {
                playPauseButton.setText("▶");
                statusLabel.setText("Paused");
            }
            case STOPPED, IDLE -> {
                playPauseButton.setText("▶");
                statusLabel.setText("Stopped");
            }
        }
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
        repeatButton.setText(switch (mode) {
            case OFF -> "🔁";
            case ALL -> "🔁";
            case ONE -> "🔂";
        });
    }

    private void updateShuffleButton() {
        boolean shuffled = controller.isShuffled();
        shuffleButton.setOpacity(shuffled ? 1.0 : 0.5);
    }

    private void showError(String message) {
        statusLabel.setText("Error: " + message);
        statusLabel.setStyle("-fx-text-fill: #ff5555;");
        
        PauseTransition pause = new PauseTransition(javafx.util.Duration.millis(5000));
        pause.setOnFinished(event -> statusLabel.setStyle("-fx-text-fill: #888888;"));
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
