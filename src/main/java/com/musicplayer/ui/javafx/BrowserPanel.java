package com.musicplayer.ui.javafx;

import com.musicplayer.domain.model.Library;
import com.musicplayer.domain.model.Track;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Panel for browsing music by category (Albums, Artists, Playlists, Genres).
 */
public class BrowserPanel extends VBox {
    
    private static final Logger logger = LoggerFactory.getLogger(BrowserPanel.class);
    
    private final TabPane tabPane;
    private final ObservableList<BrowserItem> albums;
    private final ObservableList<BrowserItem> artists;
    private final ObservableList<BrowserItem> playlists;
    private final ObservableList<BrowserItem> genres;
    
    private ListView<BrowserItem> albumListView;
    private ListView<BrowserItem> artistListView;
    private ListView<BrowserItem> playlistListView;
    private ListView<BrowserItem> genreListView;
    
    private Tab nowPlayingTab;
    private ListView<Track> nowPlayingListView;
    private final ObservableList<Track> nowPlayingTracks;
    private Label nowPlayingHeader;
    
    private List<Track> allTracks;
    private CategorySelectionListener selectionListener;
    private StartPlaylistListener startListener;
    
    public BrowserPanel() {
        setAlignment(Pos.TOP_LEFT);
        setPadding(new Insets(10));
        
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");
        
        albums = FXCollections.observableArrayList();
        artists = FXCollections.observableArrayList();
        playlists = FXCollections.observableArrayList();
        genres = FXCollections.observableArrayList();
        nowPlayingTracks = FXCollections.observableArrayList();
        
        Tab albumsTab = createTab("Albums", albums, albumListView = createListView(albums));
        Tab artistsTab = createTab("Artists", artists, artistListView = createListView(artists));
        Tab playlistsTab = createTab("Playlists", playlists, playlistListView = createListView(playlists));
        Tab genresTab = createTab("Genres", genres, genreListView = createListView(genres));
        nowPlayingTab = createNowPlayingTab();
        
        tabPane.getTabs().addAll(albumsTab, artistsTab, playlistsTab, genresTab, nowPlayingTab);
        
        setBackground(new Background(new BackgroundFill(Color.rgb(240, 240, 240), new CornerRadii(0), Insets.EMPTY)));
        getChildren().add(tabPane);
    }
    
    private Tab createNowPlayingTab() {
        Tab tab = new Tab("Now Playing");
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        nowPlayingHeader = new Label("Selection");
        nowPlayingHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Button startButton = new Button("▶ Start");
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 8 20; -fx-font-size: 14px;");
        startButton.setOnAction(e -> {
            if (startListener != null && !nowPlayingTracks.isEmpty()) {
                startListener.onStartPlaylist(List.copyOf(nowPlayingTracks));
            }
        });
        
        nowPlayingListView = new ListView<>(nowPlayingTracks);
        nowPlayingListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Track track, boolean empty) {
                super.updateItem(track, empty);
                if (empty || track == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - %s", track.getTitle(), track.getArtist()));
                    setStyle("-fx-padding: 5 10;");
                }
            }
        });
        
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        buttonBar.getChildren().add(startButton);
        
        VBox headerBox = new VBox(10);
        headerBox.getChildren().addAll(nowPlayingHeader, buttonBar);
        
        content.getChildren().addAll(headerBox, nowPlayingListView);
        VBox.setVgrow(nowPlayingListView, Priority.ALWAYS);
        
        tab.setContent(content);
        return tab;
    }
    
    private Tab createTab(String title, ObservableList<BrowserItem> items, ListView<BrowserItem> listView) {
        Tab tab = new Tab(title);
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label headerLabel = new Label(title);
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(BrowserItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox box = new VBox(5);
                    box.setAlignment(Pos.CENTER_LEFT);
                    
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    
                    ImageView thumbnail = createThumbnailImageView(item.thumbnail());
                    thumbnail.setFitWidth(40);
                    thumbnail.setFitHeight(40);
                    thumbnail.setPreserveRatio(true);
                    
                    VBox textBox = new VBox(2);
                    Label nameLabel = new Label(item.name());
                    nameLabel.setStyle("-fx-font-weight: bold;");
                    Label countLabel = new Label(item.trackCount() + " tracks");
                    countLabel.setStyle("-fx-font-size: 11px;");
                    textBox.getChildren().addAll(nameLabel, countLabel);
                    
                    hbox.getChildren().addAll(thumbnail, textBox);
                    box.getChildren().add(hbox);
                    
                    setGraphic(box);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                }
            }
        });
        
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                BrowserItem selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null && selectionListener != null) {
                    List<Track> tracks = getTracksForItem(title, selected);
                    selectionListener.onCategorySelected(title, selected, tracks);
                }
            }
        });
        
        VBox.setVgrow(listView, javafx.scene.layout.Priority.ALWAYS);
        content.getChildren().addAll(headerLabel, listView);
        
        tab.setContent(content);
        return tab;
    }
    
    private ListView<BrowserItem> createListView(ObservableList<BrowserItem> items) {
        ListView<BrowserItem> view = new ListView<>(items);
        view.setStyle("-fx-background-color: transparent;");
        return view;
    }
    
    public void setLibrary(Library library) {
        if (library == null) {
            clear();
            return;
        }
        
        allTracks = library.getEntries().stream()
                .map(entry -> new Track(
                        entry.path().toString(),
                        entry.title(),
                        entry.artist(),
                        entry.album(),
                        entry.duration()
                ))
                .toList();
        
        updateAlbums();
        updateArtists();
        updatePlaylists();
        updateGenres();
    }
    
    public void setTracks(List<Track> tracks) {
        allTracks = tracks != null ? tracks : List.of();
        updateAlbums();
        updateArtists();
        updatePlaylists();
        updateGenres();
    }
    
    private void updateAlbums() {
        albums.clear();
        if (allTracks == null) return;
        
        Map<String, List<Track>> albumGroups = allTracks.stream()
                .collect(Collectors.groupingBy(t -> t.getAlbum()));
        
        albumGroups.forEach((album, tracks) -> {
            byte[] artwork = findArtworkForAlbum(tracks);
            albums.add(new BrowserItem(album, tracks.size(), artwork));
        });
        
        albums.sort(Comparator.comparing(BrowserItem::name, String.CASE_INSENSITIVE_ORDER));
    }
    
    private void updateArtists() {
        artists.clear();
        if (allTracks == null) return;
        
        Map<String, List<Track>> artistGroups = allTracks.stream()
                .collect(Collectors.groupingBy(t -> t.getArtist()));
        
        artistGroups.forEach((artist, tracks) -> {
            byte[] artwork = findArtworkForAlbum(tracks);
            artists.add(new BrowserItem(artist, tracks.size(), artwork));
        });
        
        artists.sort(Comparator.comparing(BrowserItem::name, String.CASE_INSENSITIVE_ORDER));
    }
    
    private void updatePlaylists() {
        playlists.clear();
        if (allTracks == null) return;
        
        Map<String, List<Track>> pathGroups = new LinkedHashMap<>();
        for (Track track : allTracks) {
            String folder = extractFolderName(track.getPath());
            pathGroups.computeIfAbsent(folder, k -> new ArrayList<>()).add(track);
        }
        
        pathGroups.forEach((folder, tracks) -> {
            playlists.add(new BrowserItem(folder, tracks.size(), null));
        });
    }
    
    private void updateGenres() {
        genres.clear();
        genres.add(new BrowserItem("All Tracks", allTracks != null ? allTracks.size() : 0, null));
        genres.add(new BrowserItem("Recently Added", allTracks != null ? allTracks.size() : 0, null));
    }
    
    private List<Track> getTracksForItem(String category, BrowserItem item) {
        if (allTracks == null) return List.of();
        
        return switch (category) {
            case "Albums" -> allTracks.stream()
                    .filter(t -> item.name().equals(t.getAlbum()))
                    .toList();
            case "Artists" -> allTracks.stream()
                    .filter(t -> item.name().equals(t.getArtist()))
                    .toList();
            case "Playlists" -> {
                Map<String, List<Track>> pathGroups = new LinkedHashMap<>();
                for (Track track : allTracks) {
                    String folder = extractFolderName(track.getPath());
                    pathGroups.computeIfAbsent(folder, k -> new ArrayList<>()).add(track);
                }
                yield pathGroups.getOrDefault(item.name(), List.of());
            }
            case "Genres" -> allTracks;
            default -> List.of();
        };
    }
    
    private byte[] findArtworkForAlbum(List<Track> tracks) {
        for (Track track : tracks) {
            if (track.getPath() != null) {
                return null;
            }
        }
        return null;
    }
    
    private String extractFolderName(String path) {
        if (path == null) return "Unknown";
        int lastSep = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (lastSep < 0) return "Root";
        int prevSep = Math.max(path.lastIndexOf('/', lastSep - 1), path.lastIndexOf('\\', lastSep - 1));
        if (prevSep < 0) return "Root";
        return path.substring(prevSep + 1, lastSep);
    }
    
    public void clear() {
        albums.clear();
        artists.clear();
        playlists.clear();
        genres.clear();
        allTracks = null;
    }
    
    public void setOnCategorySelected(CategorySelectionListener listener) {
        this.selectionListener = listener;
    }
    
    public void setOnStartPlaylist(StartPlaylistListener listener) {
        this.startListener = listener;
    }
    
    public void showNowPlaying(String title, List<Track> tracks) {
        nowPlayingHeader.setText(title + " (" + tracks.size() + " tracks)");
        nowPlayingTracks.clear();
        nowPlayingTracks.addAll(tracks);
        tabPane.getSelectionModel().select(nowPlayingTab);
    }
    
    private ImageView createThumbnailImageView(byte[] thumbnail) {
        ImageView view = new ImageView();
        if (thumbnail != null && thumbnail.length > 0) {
            try {
                view.setImage(new Image(new ByteArrayInputStream(thumbnail)));
            } catch (Exception e) {
                logger.debug("Failed to load thumbnail", e);
            }
        }
        return view;
    }
    
    public record BrowserItem(String name, int trackCount, byte[] thumbnail) {
        public Image getThumbnailImage() {
            if (thumbnail != null && thumbnail.length > 0) {
                try {
                    return new Image(new ByteArrayInputStream(thumbnail));
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }
    }
    
    @FunctionalInterface
    public interface CategorySelectionListener {
        void onCategorySelected(String category, BrowserItem item, java.util.List<Track> tracks);
    }
    
    @FunctionalInterface
    public interface StartPlaylistListener {
        void onStartPlaylist(java.util.List<Track> tracks);
    }
}
