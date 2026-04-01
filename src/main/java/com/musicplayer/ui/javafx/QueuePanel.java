package com.musicplayer.ui.javafx;

import com.musicplayer.domain.model.Track;
import javafx.collections.FXCollections;
import javafx.scene.layout.Priority;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Panel displaying the current queue (playlist).
 */
public class QueuePanel extends VBox {
    
    private static final double WIDTH = 250;
    
    private final ListView<Track> trackList;
    private final ObservableList<Track> tracks;
    
    private int currentTrackIndex = -1;
    
    public QueuePanel() {
        setAlignment(Pos.TOP_LEFT);
        
        Label headerLabel = new Label("Queue");
        headerLabel.setPadding(new Insets(10, 5, 5, 10));
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        tracks = FXCollections.observableArrayList();
        trackList = new ListView<>(tracks);
        trackList.setStyle("-fx-background-color: transparent;");
        trackList.setCellFactory(lv -> createTrackCell());
        
        VBox contentBox = new VBox();
        contentBox.getChildren().addAll(headerLabel, trackList);
        VBox.setVgrow(trackList, Priority.ALWAYS);
        
        setBackground(new Background(new BackgroundFill(Color.rgb(220, 220, 220), CornerRadii.EMPTY, Insets.EMPTY)));
        getChildren().add(contentBox);
        
        setPrefWidth(WIDTH);
        setMinWidth(WIDTH);
        setMaxWidth(WIDTH);
    }
    
    private ListCell<Track> createTrackCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Track track, boolean empty) {
                super.updateItem(track, empty);
                
                if (empty || track == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%s - %s", track.getTitle(), track.getArtist()));
                    setStyle("-fx-padding: 5 10;");
                    
                    int index = getIndex();
                    if (index == currentTrackIndex) {
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-background-color: #c8e6c9; -fx-padding: 5 10;");
                    }
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
    
    @FunctionalInterface
    public interface TrackSelectedListener {
        void onTrackSelected(Track track, int index);
    }
    
    @FunctionalInterface
    public interface TrackDoubleClickedListener {
        void onTrackDoubleClicked(Track track, int index);
    }
}
