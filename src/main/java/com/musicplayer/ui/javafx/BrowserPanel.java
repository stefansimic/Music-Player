package com.musicplayer.ui.javafx;

import com.musicplayer.domain.model.Library;
import com.musicplayer.domain.model.Track;
import javafx.collections.FXCollections;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

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
    private PlayNextInQueueListener playNextInQueueListener;
    
    public BrowserPanel() {
        setAlignment(Pos.TOP_LEFT);
        setPadding(new Insets(16));
        
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-width: 0;"
        );
        tabPane.getStylesheets().clear();
        
        albums = FXCollections.observableArrayList();
        artists = FXCollections.observableArrayList();
        playlists = FXCollections.observableArrayList();
        genres = FXCollections.observableArrayList();
        nowPlayingTracks = FXCollections.observableArrayList();
        
        Tab albumsTab = createTab("Albums", albums, albumListView = createListView(albums), IconFactory.createAlbumIcon());
        Tab artistsTab = createTab("Artists", artists, artistListView = createListView(artists), IconFactory.createArtistIcon());
        Tab playlistsTab = createTab("Playlists", playlists, playlistListView = createListView(playlists), IconFactory.createPlaylistIcon());
        Tab genresTab = createTab("Genres", genres, genreListView = createListView(genres), IconFactory.createFolderIcon());
        nowPlayingTab = createNowPlayingTab();
        
        tabPane.getTabs().addAll(albumsTab, artistsTab, playlistsTab, genresTab, nowPlayingTab);
        
        setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-width: 0;"
        );
        
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        getChildren().add(tabPane);
    }
    
    private Tab createNowPlayingTab() {
        Tab tab = new Tab("Now Playing");
        tab.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px; -fx-font-weight: 500;");
        
        VBox content = new VBox(12);
        content.setPadding(new Insets(12));
        
        nowPlayingHeader = new Label("Selection");
        nowPlayingHeader.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1e293b;"
        );
        
        Button startButton = new Button("Play");
        startButton.setGraphic(IconFactory.createPlayIcon());
        ((javafx.scene.shape.SVGPath)startButton.getGraphic()).setScaleX(0.8);
        ((javafx.scene.shape.SVGPath)startButton.getGraphic()).setScaleY(0.8);
        setSvgPathColor((javafx.scene.shape.SVGPath)startButton.getGraphic(), Color.WHITE);
        startButton.setStyle(
            "-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);" +
            "-fx-text-fill: white;" +
            "-fx-padding: 10 24;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 500;" +
            "-fx-background-radius: 20;" +
            "-fx-border-radius: 20;" +
            "-fx-cursor: hand;"
        );
        startButton.setOnAction(e -> {
            if (startListener != null && !nowPlayingTracks.isEmpty()) {
                startListener.onStartPlaylist(List.copyOf(nowPlayingTracks));
            }
        });
        
        nowPlayingListView = new ListView<>(nowPlayingTracks);
        nowPlayingListView.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-width: 0;"
        );
        
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
            Track selected = nowPlayingListView.getSelectionModel().getSelectedItem();
            if (selected != null && playNextInQueueListener != null) {
                playNextInQueueListener.onPlayNextInQueue(selected);
            }
        });
        contextMenu.getItems().add(playNextItem);
        nowPlayingListView.setContextMenu(contextMenu);
        
        nowPlayingListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Track track, boolean empty) {
                super.updateItem(track, empty);
                if (empty || track == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox container = new HBox();
                    container.setSpacing(10);
                    container.setPadding(new Insets(8, 12, 8, 12));
                    container.setAlignment(Pos.CENTER_LEFT);
                    container.setStyle("-fx-background-color: transparent; -fx-background-radius: 8;");
                    
                    Label title = new Label(track.getTitle());
                    title.setStyle(
                        "-fx-font-size: 13px;" +
                        "-fx-text-fill: #334155;" +
                        "-fx-font-weight: 500;"
                    );
                    
                    Label artist = new Label(" - " + track.getArtist());
                    artist.setStyle(
                        "-fx-font-size: 12px;" +
                        "-fx-text-fill: #94a3b8;"
                    );
                    
                    container.getChildren().addAll(title, artist);
                    HBox.setHgrow(title, Priority.ALWAYS);
                    setGraphic(container);
                    setText(null);
                }
            }
        });
        
        HBox buttonBar = new HBox(12);
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        buttonBar.getChildren().add(startButton);
        
        VBox headerBox = new VBox(10);
        headerBox.getChildren().addAll(nowPlayingHeader, buttonBar);
        
        content.getChildren().addAll(headerBox, nowPlayingListView);
        VBox.setVgrow(nowPlayingListView, Priority.ALWAYS);
        
        tab.setContent(content);
        return tab;
    }
    
    private Tab createTab(String title, ObservableList<BrowserItem> items, ListView<BrowserItem> listView, Node icon) {
        Tab tab = new Tab(title);
        tab.setStyle(
            "-fx-text-fill: #64748b;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: 500;" +
            "-fx-padding: 10 20;"
        );
        
        VBox content = new VBox(12);
        content.setPadding(new Insets(12));
        
        Label headerLabel = new Label(title);
        headerLabel.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1e293b;"
        );
        
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(BrowserItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(12);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.setPadding(new Insets(10, 12, 10, 12));
                    hbox.setStyle(
                        "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.04), 4, 0, 0, 2);"
                    );
                    
                    ImageView thumbnail = createThumbnailImageView(item.thumbnail());
                    thumbnail.setFitWidth(44);
                    thumbnail.setFitHeight(44);
                    thumbnail.setPreserveRatio(true);
                    thumbnail.setSmooth(true);
                    thumbnail.setStyle(
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;"
                    );
                    
                    VBox textBox = new VBox(3);
                    textBox.setAlignment(Pos.CENTER_LEFT);
                    
                    Label nameLabel = new Label(item.name());
                    nameLabel.setStyle(
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-text-fill: #334155;" +
                        "-fx-text-smoothing-type: lcd;"
                    );
                    HBox.setHgrow(nameLabel, Priority.ALWAYS);
                    
                    Label countLabel = new Label(item.trackCount() + " tracks");
                    countLabel.setStyle(
                        "-fx-font-size: 12px;" +
                        "-fx-text-fill: #94a3b8;"
                    );
                    
                    textBox.getChildren().addAll(nameLabel, countLabel);
                    HBox.setHgrow(textBox, Priority.ALWAYS);
                    hbox.getChildren().addAll(thumbnail, textBox);
                    
                    hbox.setOnMouseEntered(e -> 
                        hbox.setStyle(
                            "-fx-background-color: #f8fafc;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: #e2e8f0;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 10;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.06), 6, 0, 0, 3);"
                        )
                    );
                    hbox.setOnMouseExited(e -> 
                        hbox.setStyle(
                            "-fx-background-color: white;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: transparent;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 10;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.04), 4, 0, 0, 2);"
                        )
                    );
                    
                    setGraphic(hbox);
                    setText(null);
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
        
        VBox.setVgrow(listView, Priority.ALWAYS);
        content.getChildren().addAll(headerLabel, listView);
        
        tab.setContent(content);
        return tab;
    }
    
    private ListView<BrowserItem> createListView(ObservableList<BrowserItem> items) {
        ListView<BrowserItem> view = new ListView<>(items);
        view.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-width: 0;" +
            "-fx-padding: 0;"
        );
        view.setFixedCellSize(-1);
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
                        entry.duration(),
                        entry.artwork()
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
            byte[] artwork = findArtworkForAlbum(tracks);
            playlists.add(new BrowserItem(folder, tracks.size(), artwork));
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
            if (track.hasArtwork()) {
                return track.getArtwork();
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
    
    public void setOnPlayNext(PlayNextInQueueListener listener) {
        this.playNextInQueueListener = listener;
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
                view.setImage(createPlaceholderImage());
            }
        } else {
            view.setImage(createPlaceholderImage());
        }
        return view;
    }
    
    private Image createPlaceholderImage() {
        int width = 44;
        int height = 44;
        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();
        
        Color primaryColor = Color.rgb(102, 126, 234);
        Color secondaryColor = Color.rgb(118, 75, 162);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double gradient = (double) x / width;
                int r = (int)(102 + (118 - 102) * gradient);
                int g = (int)(126 + (75 - 126) * gradient);
                int b = (int)(234 + (162 - 234) * gradient);
                writer.setColor(x, y, Color.rgb(r, g, b));
            }
        }
        
        int centerX = 22;
        int centerY = 16;
        int radius = 10;
        for (int y = centerY - radius; y <= centerY + radius; y++) {
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                if (y >= 0 && y < height && x >= 0 && x < width) {
                    double dist = Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));
                    if (dist <= radius) {
                        writer.setColor(x, y, Color.rgb(255, 255, 255));
                    }
                }
            }
        }
        
        return image;
    }
    
    private void setSvgPathColor(javafx.scene.shape.SVGPath path, Color color) {
        path.setFill(color);
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
    
    @FunctionalInterface
    public interface PlayNextInQueueListener {
        void onPlayNextInQueue(Track track);
    }
}
