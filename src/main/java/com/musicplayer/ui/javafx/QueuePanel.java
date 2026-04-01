package com.musicplayer.ui.javafx;

import com.musicplayer.domain.model.Track;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.scene.layout.Priority;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Collapsible panel displaying the current queue (playlist).
 */
public class QueuePanel extends VBox {
    
    private static final double EXPANDED_WIDTH = 250;
    private static final double COLLAPSED_WIDTH = 40;
    private static final double ANIMATION_DURATION = 200;
    
    private final ListView<Track> trackList;
    private final ObservableList<Track> tracks;
    private final Button toggleButton;
    private final Label headerLabel;
    
    private boolean isCollapsed = false;
    private int currentTrackIndex = -1;
    private TrackChangeListener trackChangeListener;
    
    public QueuePanel() {
        setAlignment(Pos.TOP_LEFT);
        
        headerLabel = new Label("Queue");
        headerLabel.setPadding(new Insets(10, 5, 5, 5));
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        toggleButton = new Button("◀");
        toggleButton.setStyle("-fx-background-color: transparent; -fx-font-size: 12px;");
        toggleButton.setOnAction(e -> toggleCollapse());
        
        VBox headerBox = new VBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(5));
        headerBox.getChildren().addAll(toggleButton, headerLabel);
        
        tracks = FXCollections.observableArrayList();
        trackList = new ListView<>(tracks);
        trackList.setStyle("-fx-background-color: transparent;");
        trackList.setCellFactory(lv -> createTrackCell());
        
        VBox contentBox = new VBox();
        contentBox.getChildren().addAll(headerBox, trackList);
        VBox.setVgrow(trackList, Priority.ALWAYS);
        
        setBackground(new Background(new BackgroundFill(Color.rgb(220, 220, 220), CornerRadii.EMPTY, Insets.EMPTY)));
        getChildren().add(contentBox);
        
        setPrefWidth(EXPANDED_WIDTH);
        setMinWidth(COLLAPSED_WIDTH);
        setMaxWidth(EXPANDED_WIDTH);
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
    
    public void toggleCollapse() {
        isCollapsed = !isCollapsed;
        animateWidth();
    }
    
    public void expand() {
        if (isCollapsed) {
            isCollapsed = false;
            animateWidth();
        }
    }
    
    public void collapse() {
        if (!isCollapsed) {
            isCollapsed = true;
            animateWidth();
        }
    }
    
    public boolean isCollapsed() {
        return isCollapsed;
    }
    
    private void animateWidth() {
        TranslateTransition slideTransition = new TranslateTransition(
                Duration.millis(ANIMATION_DURATION), this);
        
        if (isCollapsed) {
            slideTransition.setToX(EXPANDED_WIDTH - COLLAPSED_WIDTH);
            toggleButton.setText("▶");
            headerLabel.setVisible(false);
            trackList.setVisible(false);
        } else {
            slideTransition.setToX(0);
            toggleButton.setText("◀");
            headerLabel.setVisible(true);
            trackList.setVisible(true);
        }
        
        slideTransition.play();
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
    public interface TrackChangeListener {
        void onTrackChanged(Track track);
    }
}
