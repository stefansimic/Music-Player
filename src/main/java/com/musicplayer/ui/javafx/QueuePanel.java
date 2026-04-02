package com.musicplayer.ui.javafx;

import com.musicplayer.domain.model.Track;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class QueuePanel extends VBox {
    
    private static final double WIDTH = 240;
    
    private final ListView<Track> trackList;
    private final ObservableList<Track> tracks;
    
    private int currentTrackIndex = -1;
    private PlayNextListener playNextListener;
    private RemoveTrackListener removeTrackListener;
    
    public QueuePanel() {
        setAlignment(Pos.TOP_LEFT);
        
        Label headerLabel = new Label("Queue");
        headerLabel.setPadding(new Insets(12, 12, 8, 12));
        headerLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-font-weight: 600;" +
            "-fx-text-fill: #64748b;" +
            "-fx-font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;"
        );
        
        tracks = FXCollections.observableArrayList();
        trackList = new ListView<>(tracks);
        trackList.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-width: 0;" +
            "-fx-padding: 0;"
        );
        trackList.setCellFactory(lv -> createTrackCell());
        trackList.setFixedCellSize(-1);
        
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0, 0, 4);"
        );
        
        MenuItem playNextItem = new MenuItem("Play next");
        playNextItem.setStyle(
            "-fx-text-fill: #334155;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 8 16;"
        );
        playNextItem.setOnAction(e -> {
            Track selected = trackList.getSelectionModel().getSelectedItem();
            if (selected != null && playNextListener != null) {
                playNextListener.onPlayNext(selected);
            }
        });
        
        MenuItem removeItem = new MenuItem("Remove");
        removeItem.setStyle(
            "-fx-text-fill: #ef4444;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 8 16;"
        );
        removeItem.setOnAction(e -> {
            Track selected = trackList.getSelectionModel().getSelectedItem();
            int index = trackList.getSelectionModel().getSelectedIndex();
            if (selected != null && removeTrackListener != null) {
                removeTrackListener.onRemoveTrack(selected, index);
            }
        });
        
        contextMenu.getItems().addAll(playNextItem, new SeparatorMenuItem(), removeItem);
        
        trackList.setContextMenu(contextMenu);
        
        VBox contentBox = new VBox();
        contentBox.setFillWidth(true);
        contentBox.getChildren().addAll(headerLabel, trackList);
        VBox.setVgrow(trackList, Priority.ALWAYS);
        
        setStyle(
            "-fx-background-color: #f8fafc;" +
            "-fx-border-width: 0 1 0 0;" +
            "-fx-border-color: #e2e8f0;"
        );
        setPrefWidth(WIDTH);
        setMinWidth(WIDTH);
        setMaxWidth(WIDTH);
        
        VBox.setVgrow(this, Priority.ALWAYS);
        VBox.setVgrow(contentBox, Priority.ALWAYS);
        
        getChildren().add(contentBox);
    }
    
    private ListCell<Track> createTrackCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Track track, boolean empty) {
                super.updateItem(track, empty);
                
                if (empty || track == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    VBox container = new VBox(2);
                    container.setAlignment(Pos.CENTER_LEFT);
                    container.setPadding(new Insets(10, 12, 10, 12));
                    
                    Label title = new Label(track.getTitle());
                    title.setStyle(
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 500;" +
                        "-fx-text-fill: #334155;" +
                        "-fx-font-smoothing-type: lcd;" +
                        "-fx-max-width: Infinity;"
                    );
                    title.setMaxWidth(Double.MAX_VALUE);
                    
                    Label artist = new Label(track.getArtist());
                    artist.setStyle(
                        "-fx-font-size: 11px;" +
                        "-fx-text-fill: #94a3b8;"
                    );
                    
                    container.getChildren().addAll(title, artist);
                    
                    int index = getIndex();
                    if (index == currentTrackIndex) {
                        container.setStyle(
                            "-fx-background-color: rgba(102, 126, 234, 0.1);" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-color: rgba(102, 126, 234, 0.3);" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 8;"
                        );
                        title.setStyle(
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: 600;" +
                            "-fx-text-fill: #667eea;" +
                            "-fx-font-smoothing-type: lcd;" +
                            "-fx-max-width: Infinity;"
                        );
                    } else {
                        container.setStyle(
                            "-fx-background-color: transparent;" +
                            "-fx-background-radius: 8;"
                        );
                    }
                    
                    setGraphic(container);
                    setText(null);
                }
            }
        };
    }
    
    public void setTracks(java.util.List<Track> newTracks) {
        tracks.clear();
        if (newTracks != null) {
            tracks.addAll(newTracks);
        }
    }
    
    public void setCurrentTrackIndex(int index) {
        int oldIndex = currentTrackIndex;
        currentTrackIndex = index;
        
        if (oldIndex >= 0 && oldIndex < tracks.size()) {
            trackList.refresh();
        }
        if (index >= 0 && index < tracks.size()) {
            trackList.getSelectionModel().select(index);
            trackList.scrollTo(index);
            trackList.refresh();
        }
    }
    
    public void setOnTrackSelected(TrackSelectedListener listener) {
        trackList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && listener != null) {
                int index = tracks.indexOf(newVal);
                listener.onTrackSelected(newVal, index);
            }
        });
    }
    
    public void setOnTrackDoubleClicked(TrackDoubleClickedListener listener) {
        trackList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Track selected = trackList.getSelectionModel().getSelectedItem();
                if (selected != null && listener != null) {
                    int index = tracks.indexOf(selected);
                    listener.onTrackDoubleClicked(selected, index);
                }
            }
        });
    }
    
    public void setOnPlayNext(PlayNextListener listener) {
        this.playNextListener = listener;
    }
    
    public void setOnRemoveTrack(RemoveTrackListener listener) {
        this.removeTrackListener = listener;
    }
    
    @FunctionalInterface
    public interface TrackSelectedListener {
        void onTrackSelected(Track track, int index);
    }
    
    @FunctionalInterface
    public interface TrackDoubleClickedListener {
        void onTrackDoubleClicked(Track track, int index);
    }
    
    @FunctionalInterface
    public interface PlayNextListener {
        void onPlayNext(Track track);
    }
    
    @FunctionalInterface
    public interface RemoveTrackListener {
        void onRemoveTrack(Track track, int index);
    }
}
