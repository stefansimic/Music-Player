package com.musicplayer.ui.javafx;

import com.musicplayer.application.controller.PlayerController;
import com.musicplayer.domain.model.PlaybackState;
import com.musicplayer.domain.model.RepeatMode;
import com.musicplayer.domain.model.Track;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * Main JavaFX UI for the Music Player application.
 */
public class MusicPlayerUI extends VBox {
    
    private static final Logger logger = LoggerFactory.getLogger(MusicPlayerUI.class);
    
    private final PlayerController controller;
    
    private ListView<Track> playlistView;
    private Label titleLabel;
    private Label artistLabel;
    private Label albumLabel;
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
    }

    private void setupUI() {
        setSpacing(10);
        setPadding(new Insets(15));
        setAlignment(Pos.CENTER);
        
        MenuBar menuBar = createMenuBar();
        
        VBox trackInfoBox = createTrackInfoPanel();
        
        HBox progressBox = createProgressPanel();
        
        HBox controlsBox = createControlsPanel();
        
        HBox volumeBox = createVolumePanel();
        
        playlistView = createPlaylistView();
        
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: gray;");
        
        getChildren().addAll(
            menuBar,
            trackInfoBox,
            progressBox,
            controlsBox,
            volumeBox,
            playlistView,
            statusLabel
        );
        
        VBox.setVgrow(playlistView, Priority.ALWAYS);
        
        setStyle("-fx-font-family: 'System', 'Arial Unicode MS', 'Lucida Grande', sans-serif;");
    }

    private MenuBar createMenuBar() {
        Menu fileMenu = new Menu("File");
        
        MenuItem openDir = new MenuItem("Open Directory");
        openDir.setOnAction(e -> openDirectory());
        openDir.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+O"));
        
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> Platform.exit());
        
        fileMenu.getItems().addAll(openDir, new SeparatorMenuItem(), exit);
        
        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About");
        about.setOnAction(e -> showAbout());
        helpMenu.getItems().add(about);
        
        return new MenuBar(fileMenu, helpMenu);
    }

    private VBox createTrackInfoPanel() {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        
        titleLabel = new Label("No track loaded");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        artistLabel = new Label("");
        artistLabel.setStyle("-fx-font-size: 14px;");
        
        albumLabel = new Label("");
        albumLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
        
        box.getChildren().addAll(titleLabel, artistLabel, albumLabel);
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

    private HBox createControlsPanel() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER);
        
        previousButton = new Button("⏮");
        previousButton.setStyle("-fx-font-size: 20px;");
        
        playPauseButton = new Button("▶");
        playPauseButton.setStyle("-fx-font-size: 30px;");
        
        nextButton = new Button("⏭");
        nextButton.setStyle("-fx-font-size: 20px;");
        
        shuffleButton = new Button("🔀");
        shuffleButton.setStyle("-fx-font-size: 16px;");
        
        repeatButton = new Button("🔁");
        repeatButton.setStyle("-fx-font-size: 16px;");
        
        box.getChildren().addAll(shuffleButton, previousButton, playPauseButton, nextButton, repeatButton);
        return box;
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

    private ListView<Track> createPlaylistView() {
        ListView<Track> view = new ListView<>();
        
        TableColumn<Track, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setPrefWidth(200);
        
        TableColumn<Track, String> artistColumn = new TableColumn<>("Artist");
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        artistColumn.setPrefWidth(150);
        
        TableColumn<Track, String> durationColumn = new TableColumn<>("Duration");
        durationColumn.setPrefWidth(80);
        durationColumn.setCellValueFactory(cellData -> {
            Duration duration = cellData.getValue().getDuration();
            String durationStr = duration != null ? formatDuration(duration) : "--:--";
            return new javafx.beans.property.SimpleStringProperty(durationStr);
        });
        
        TableView<Track> table = new TableView<>();
        table.getColumns().addAll(titleColumn, artistColumn, durationColumn);
        
        view.setCellFactory(lv -> {
            ListCell<Track> cell = new ListCell<>() {
                @Override
                protected void updateItem(Track track, boolean empty) {
                    super.updateItem(track, empty);
                    if (empty || track == null) {
                        setText(null);
                    } else {
                        setText(String.format("%s - %s", track.getTitle(), track.getArtist()));
                    }
                }
            };
            
            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !cell.isEmpty()) {
                    int index = cell.getIndex();
                    controller.playTrack(index);
                }
            });
            
            return cell;
        });
        
        return view;
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
                Platform.runLater(() -> updateTrackInfo(track));
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
                Platform.runLater(() -> updatePlaylist(playlist));
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
        });
        
        volumeSlider.setValue(controller.getVolume() * 100);
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
                    controller.loadDirectory(dir);
                }
            }
            event.consume();
        });
    }

    private void openDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Music Directory");
        
        Stage stage = (Stage) getScene().getWindow();
        java.io.File result = chooser.showDialog(stage);
        
        if (result != null) {
            controller.loadDirectory(result.toPath());
        }
    }

    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Music Player");
        alert.setHeaderText("Music Player v1.0.0");
        alert.setContentText("A simple music player for MP3 files.\n\nBuilt with Java and JavaFX.");
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
            albumLabel.setText(track.getAlbum());
            
            int currentIndex = controller.getCurrentIndex();
            highlightCurrentTrack(currentIndex);
        } else {
            titleLabel.setText("No track loaded");
            artistLabel.setText("");
            albumLabel.setText("");
        }
    }

    private void highlightCurrentTrack(int index) {
        if (index >= 0 && index < playlistView.getItems().size()) {
            playlistView.getSelectionModel().select(index);
            playlistView.scrollTo(index);
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

    private void updatePlaylist(List<Track> playlist) {
        playlistView.getItems().clear();
        playlistView.getItems().addAll(playlist);
    }

    private void updateRepeatButton() {
        RepeatMode mode = controller.getRepeatMode();
        repeatButton.setText(switch (mode) {
            case OFF -> "🔁";
            case ALL -> "🔁";
            case ONE -> "🔂";
        });
        repeatButton.setStyle("-fx-font-size: 16px; " + 
            (mode == RepeatMode.OFF ? "" : "-fx-opacity: 1.0; -fx-background-color: #e0e0e0;"));
    }

    private void updateShuffleButton() {
        boolean shuffled = controller.isShuffled();
        shuffleButton.setStyle("-fx-font-size: 16px; " + 
            (shuffled ? "-fx-opacity: 1.0; -fx-background-color: #e0e0e0;" : ""));
    }

    private void showError(String message) {
        statusLabel.setText("Error: " + message);
        statusLabel.setStyle("-fx-text-fill: red;");
        
        javafx.application.Platform.runLater(() -> {
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                javafx.application.Platform.runLater(() -> 
                    statusLabel.setStyle("-fx-text-fill: gray;"));
            }).start();
        });
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
