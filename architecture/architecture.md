# Music Player - Architecture Document

**Version:** 2.0.0  
**Date:** 2026-04-01  
**Status:** Draft

---

## Overview

This document describes the software architecture for the Music Player application. It defines the high-level structure, layer separation, module organization, and key design decisions to ensure maintainability, testability, and extensibility.

---

## 1. Design Principles

The architecture follows these principles:

| Principle | Description |
|-----------|-------------|
| **Separation of Concerns** | UI layer is completely separate from business logic |
| **Dependency Inversion** | High-level modules do not depend on low-level modules |
| **Single Responsibility** | Each class has one well-defined purpose |
| **Interface Segregation** | Small, focused interfaces instead of large ones |
| **Testability** | Business logic can be tested without UI dependencies |

---

## 2. High-Level Architecture

### 2.1 Layered Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                       │
│                         (ui.*)                               │
│                                                              │
│    Handles all user interface components. This layer is      │
│    completely swappable - can use Swing, JavaFX, or CLI.    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                          │
│                     (application.*)                          │
│                                                              │
│    Contains application services and the controller facade.  │
│    Orchestrates domain logic and coordinates services.       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       DOMAIN LAYER                           │
│                      (domain.*)                              │
│                                                              │
│    Contains business entities and contracts (interfaces).    │
│    This layer has ZERO external dependencies.                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   INFRASTRUCTURE LAYER                       │
│                   (infrastructure.*)                         │
│                                                              │
│    Contains concrete implementations of domain contracts.     │
│    Audio players, file scanners, metadata readers.            │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Dependency Rule

```
UI ──► Application ──► Domain ◄── Infrastructure
                      (interfaces)
```

- UI depends on Application
- Application depends on Domain interfaces
- Infrastructure implements Domain interfaces
- Domain has **no** external dependencies

---

## 3. Package Structure

```
src/main/java/com/musicplayer/
│
├── Main.java
│
├── domain/
│   ├── model/
│   │   ├── Track.java              # Track entity
│   │   ├── Playlist.java            # Playlist/Queue entity
│   │   ├── RepeatMode.java         # Repeat mode enum
│   │   └── Library.java            # Library model (v2.0)
│   │
│   └── contract/
│       ├── AudioPlayer.java        # Audio playback contract
│       ├── FileScanner.java         # File discovery contract
│       ├── MetadataReader.java      # Metadata extraction contract
│       └── LibraryRepository.java   # Library persistence contract (v2.0)
│
├── application/
│   ├── service/
│   │   ├── AudioService.java       # Playback orchestration
│   │   ├── PlaylistService.java     # Queue, shuffle, repeat
│   │   ├── FileService.java         # Directory import logic
│   │   ├── MetadataService.java     # Metadata reading
│   │   └── LibraryService.java      # Library persistence (v2.0)
│   │
│   └── controller/
│       └── PlayerController.java    # Facade for UI
│
├── infrastructure/
│   ├── audio/
│   │   └── JavaFxAudioPlayer.java  # JavaFX implementation
│   │
│   ├── filesystem/
│   │   └── NioFileScanner.java      # NIO file discovery
│   │
│   ├── metadata/
│   │   └── JAudioTaggerReader.java   # ID3 tag reading (v2.0: + artwork)
│   │
│   └── persistence/
│       └── JsonLibraryRepository.java # JSON library storage (v2.0)
│
└── ui/
    └── javafx/
        ├── MusicPlayerUI.java       # Main JavaFX UI (v2.0: redesigned layout)
        ├── BrowserPanel.java        # Browse by Albums/Artists/Playlists (v2.0)
        ├── QueuePanel.java          # Collapsible queue panel (v2.0)
        ├── ControlsPanel.java       # Playback controls (v2.0)
        └── ArtworkPanel.java        # Album artwork display (v2.0)
```

---

## 4. Domain Layer

### 4.1 Models

#### Track
```java
public class Track {
    private final String path;          // File path (immutable)
    private String title;               // From metadata or filename
    private String artist;              // From ID3 tag
    private String album;               // From ID3 tag
    private Duration duration;          // From metadata
    private byte[] artwork;             // Album artwork bytes (v2.0)
    private long lastModified;           // File modification timestamp for change detection (v2.0)
}
```

#### Playlist
```java
public class Playlist {
    private String name;
    private List<Track> tracks;
    private int currentIndex;            // -1 if no track selected
    private RepeatMode repeatMode;
    private boolean shuffled;
}
```

#### RepeatMode
```java
public enum RepeatMode {
    OFF,    // Stop after last track
    ALL,    // Loop back to first
    ONE     // Repeat current track
}
```

#### Library (v2.0)
```java
public class Library {
    private List<Path> importPaths;     // Monitored directory paths
    private List<LibraryEntry> entries;  // Tracks with cached metadata
    private long lastScanTimestamp;      // Last full scan time
}

public record LibraryEntry(
    Path path,
    String title,
    String artist,
    String album,
    Duration duration,
    byte[] artwork,
    long lastModified
) {}
```

### 4.2 Contracts (Interfaces)

#### AudioPlayer
```java
public interface AudioPlayer {
    void play(Track track);
    void pause();
    void resume();
    void stop();
    void seek(Duration position);
    void setVolume(double volume);      // 0.0 to 1.0
    
    boolean isPlaying();
    Duration getCurrentPosition();
    Duration getDuration();
    
    void addPlaybackListener(PlaybackListener listener);
    void removePlaybackListener(PlaybackListener listener);
}
```

#### FileScanner
```java
public interface FileScanner {
    List<Path> scanDirectory(Path directory, boolean recursive);
    List<Path> scanAndSort(Path directory, boolean recursive);
}
```

#### MetadataReader
```java
public interface MetadataReader {
    TrackMetadata read(Path filePath) throws MetadataException;
    byte[] readArtwork(Path filePath) throws MetadataException;  // v2.0
}

public record TrackMetadata(
    String title,
    String artist,
    String album,
    Duration duration,
    byte[] artwork  // v2.0: included in metadata
) {}
```

#### LibraryRepository (v2.0)
```java
public interface LibraryRepository {
    void save(Library library) throws LibraryStorageException;
    Library load() throws LibraryStorageException;
    boolean exists();
    void delete() throws LibraryStorageException;
}

public class LibraryStorageException extends MusicPlayerException {
    // Thrown when library save/load operations fail
}
```

---

## 5. Application Layer

### 5.1 PlayerController (Facade)

The main entry point for the UI layer. Coordinates all services.

```java
public class PlayerController {
    private final AudioService audioService;
    private final PlaylistService playlistService;
    private final FileService fileService;
    private final MetadataService metadataService;
    private final LibraryService libraryService;  // v2.0
    
    // Playback control
    public void play() { }
    public void pause() { }
    public void stop() { }
    public void next() { }
    public void previous() { }
    public void seek(Duration position) { }
    public void setVolume(double volume) { }
    
    // Playlist control
    public void loadDirectory(Path directory) { }
    public void playTrack(int index) { }
    public void setRepeatMode(RepeatMode mode) { }
    public void toggleShuffle() { }
    
    // Library control (v2.0)
    public void addImportPath(Path path) { }
    public void removeImportPath(Path path) { }
    public List<Path> getImportPaths() { }
    public void rescanLibrary() { }  // Force full rescan
    public Library getLibrary() { }
    
    // State access
    public Track getCurrentTrack() { }
    public List<Track> getPlaylist() { }
    public PlaybackState getPlaybackState() { }
    
    // Listeners
    public void addStateListener(PlayerStateListener listener) { }
}
```

### 5.2 Services

| Service | Responsibility |
|---------|----------------|
| `AudioService` | Wraps AudioPlayer, manages playback lifecycle |
| `PlaylistService` | Queue management, shuffle, repeat mode logic |
| `FileService` | Recursive directory scanning, sorting |
| `MetadataService` | Batch metadata reading, error handling |
| `LibraryService` | Library persistence, incremental rescan (v2.0) |

---

## 6. Infrastructure Layer

### 6.1 JavaFX Audio Player

Implementation using `javafx.scene.media.Media` and `MediaPlayer`.

```java
public class JavaFxAudioPlayer implements AudioPlayer {
    private MediaPlayer mediaPlayer;
    private final List<PlaybackListener> listeners = new CopyOnWriteArrayList<>();
    
    @Override
    public void play(Track track) {
        // Create Media from file path
        // Create MediaPlayer
        // Register listeners
        // Start playback
    }
    
    @Override
    public void addPlaybackListener(PlaybackListener listener) {
        listeners.add(listener);
    }
}
```

### 6.2 NIO File Scanner

Recursive directory scanning with proper resource handling.

```java
public class NioFileScanner implements FileScanner {
    private static final Predicate<Path> MP3_FILTER = 
        path -> path.toString().toLowerCase().endsWith(".mp3");
    
    @Override
    public List<Path> scanAndSort(Path directory, boolean recursive) {
        // Use Files.walk() for recursive scanning
        // Filter for .mp3 files
        // Sort alphabetically (natural order, case-insensitive)
        // Return immutable list
    }
}
```

### 6.3 JAudioTagger Metadata Reader

ID3 tag extraction using JAudioTagger library.

```java
public class JAudioTaggerReader implements MetadataReader {
    @Override
    public TrackMetadata read(Path filePath) throws MetadataException {
        try {
            AudioFile audioFile = AudioFileIO.read(filePath.toFile());
            AudioHeader header = audioFile.getAudioHeader();
            Tag tag = audioFile.getTag();
            byte[] artwork = extractArtwork(tag);  // v2.0
            
            return new TrackMetadata(
                tag.getFirst(FieldKey.TITLE),
                tag.getFirst(FieldKey.ARTIST),
                tag.getFirst(FieldKey.ALBUM),
                Duration.ofSeconds(header.getTrackLength()),
                artwork  // v2.0
            );
        } catch (Exception e) {
            throw new MetadataException("Failed to read metadata", e);
        }
    }
    
    @Override
    public byte[] readArtwork(Path filePath) throws MetadataException {
        // Extract album artwork from ID3 tag
        // Returns null if no artwork available
    }
}
```

### 6.4 JSON Library Repository (v2.0)

JSON-based implementation for library persistence.

```java
public class JsonLibraryRepository implements LibraryRepository {
    private static final Path LIBRARY_FILE = 
        Paths.get(System.getProperty("user.home"), ".musicplayer", "library.json");
    
    private final ObjectMapper objectMapper;
    private final FileService fileService;
    
    @Override
    public void save(Library library) throws LibraryStorageException {
        // Backup existing file
        // Write new library JSON atomically
        // Use pretty printing for human readability
    }
    
    @Override
    public Library load() throws LibraryStorageException {
        // Read and deserialize JSON
        // Validate entries
        // Return empty library if file doesn't exist
    }
    
    @Override
    public boolean exists() {
        return Files.exists(LIBRARY_FILE);
    }
    
    @Override
    public void delete() throws LibraryStorageException {
        Files.deleteIfExists(LIBRARY_FILE);
    }
}
```

---

## 7. UI Layer (v2.0)

### 7.1 JavaFX UI Structure (v2.0)

New layout with BorderPane structure:

```
MusicPlayerUI (BorderPane)
│
├── LEFT: QueuePanel (collapsible)
│   ├── ToggleButton (collapse/expand)
│   ├── TrackListView
│   │   └── TrackCell (title, artist, duration)
│   └── Current track highlighted
│
├── CENTER: BrowserPanel
│   ├── TabPane
│   │   ├── Albums tab
│   │   ├── Artists tab
│   │   ├── Playlists tab (folders as playlists)
│   │   └── Genres tab
│   └── Content area for selected category
│
├── BOTTOM: ControlsPanel
│   ├── TrackInfoLabel (title - artist)
│   ├── ProgressBar (with seeking)
│   ├── TimeLabels (current / total)
│   ├── ControlsHBox (prev, play/pause, next)
│   ├── ModeButtons (shuffle, repeat)
│   └── VolumeSlider
│
└── BOTTOM-RIGHT: ArtworkPanel
    ├── ImageView (album art)
    └── PlaceholderImage (when no art)
```

### 7.2 UI-Domain Communication

UI observes state changes via listeners:

```java
public interface PlayerStateListener {
    void onTrackChanged(Track track);
    void onPlaybackStateChanged(PlaybackState state);
    void onProgressChanged(Duration position, Duration duration);
    void onVolumeChanged(double volume);
    void onPlaylistChanged(List<Track> playlist);
    void onLibraryChanged(Library library);  // v2.0
    void onError(String message);
}
```

### 7.3 Queue Panel (v2.0)

Collapsible queue panel on left side:

```java
public class QueuePanel extends VBox {
    private final ToggleButton collapseButton;
    private final ListView<Track> trackList;
    private boolean isCollapsed;
    
    public void toggleCollapse() {
        // Animate width transition
        // Toggle between expanded (250px) and collapsed (40px)
    }
}
```

### 7.4 Browser Panel (v2.0)

Browse music by category:

```java
public class BrowserPanel extends VBox {
    private final TabPane tabPane;
    private final AlbumBrowser albumsTab;
    private final ArtistBrowser artistsTab;
    private final PlaylistBrowser playlistsTab;  // Folders treated as playlists
    private final GenreBrowser genresTab;
}

public interface BrowserCategory {
    String getName();
    List<BrowserItem> getItems();  // Albums, Artists, Playlists, Genres
    void onItemSelected(BrowserItem item);
}

public record BrowserItem(
    String name,
    int trackCount,
    Image thumbnail  // Album art or placeholder
) {}
```

### 7.5 Artwork Panel (v2.0)

Album artwork display with fallback:

```java
public class ArtworkPanel extends VBox {
    private final ImageView imageView;
    private static final Image PLACEHOLDER_IMAGE;
    
    public void setArtwork(byte[] artworkBytes) {
        if (artworkBytes != null && artworkBytes.length > 0) {
            Image image = new Image(new ByteArrayInputStream(artworkBytes));
            imageView.setImage(image);
        } else {
            imageView.setImage(PLACEHOLDER_IMAGE);
        }
    }
}
```

---

## 8. Error Handling Strategy

### 8.1 Error Categories

| Category | Handling |
|----------|----------|
| **Corrupted file** | Log warning, skip to next track, notify UI |
| **File not found** | Log error, remove from playlist, notify UI |
| **Metadata read failure** | Use filename as title, "Unknown" for artist/album |
| **Playback failure** | Log error, attempt next track, notify UI |
| **Directory access denied** | Show error dialog, abort import |

### 8.2 Error Notification Flow

```
Infrastructure Layer
        │
        ▼ (catch exception)
Application Layer
        │
        ▼ (log + convert to domain event)
    UI Layer
        │
        ▼ (display to user)
  User Notification
```

---

## 9. Threading Model

```
┌─────────────────────────────────────────────────────────────┐
│                        JavaFX Application Thread             │
│                                                              │
│  • UI event handling                                         │
│  • Use Platform.runLater() to update UI from other threads  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Audio Playback Thread                    │
│                     (JavaFX MediaPlayer internal)            │
│                                                              │
│  • Position updates via listener callbacks                   │
│  • Track end notifications                                   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Background Executor                       │
│                                                              │
│  • File scanning (ForkJoinPool.commonPool)                    │
│  • Metadata reading (parallel, configurable)                  │
│  • Results posted back to UI via Platform.runLater()        │
└─────────────────────────────────────────────────────────────┘
```

---

## 10. Design Patterns

| Pattern | Location | Purpose |
|---------|---------|---------|
| **Facade** | `PlayerController` | Simple API for complex subsystem |
| **Observer** | `PlaybackListener` | Decouple playback state from UI |
| **Strategy** | `AudioPlayer`, `FileScanner` | Swappable implementations |
| **Factory** | Service creation | Loose coupling for implementations |
| **Builder** | `Track`, `TrackMetadata` | Complex object construction |
| **DTO** | `TrackMetadata` | Data transfer between layers |

---

## 11. Dependencies

### 11.1 External Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| JavaFX Media | Built-in (Java 21) | Audio playback |
| JAudioTagger | 3.0.1 | MP3 metadata (ID3 tags) |
| Jackson | 2.16.1 | JSON serialization for library persistence (v2.0) |
| SLF4J | 2.0.13 | Logging facade |
| Logback | 1.5.6 | Logging implementation |
| JUnit | 5.10.2 | Testing |
| Mockito | 5.11.0 | Mocking in tests |
| AssertJ | 3.25.3 | Fluent assertions |

### 11.2 Required Dependencies (pom.xml)

```xml
<dependency>
    <groupId>net.jthink</groupId>
    <artifactId>jaudiotagger</artifactId>
    <version>3.0.1</version>
</dependency>

<!-- v2.0: JSON serialization for library persistence -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.16.1</version>
</dependency>
```

---

## 11.3 Exception Hierarchy

Domain exceptions have **no infrastructure dependencies** (no external library exceptions leak into domain).

```
domain.exception.MusicPlayerException (abstract)
├── FileAccessException     # File cannot be read/moved/deleted
├── MetadataException       # Metadata extraction failed
├── PlaybackException       # Audio playback failed
├── PlaylistException       # Playlist operations failed (e.g., empty playlist)
└── LibraryStorageException # Library persistence failed (v2.0)
```

### Exception Package Location

```
src/main/java/com/musicplayer/
└── domain/
    └── exception/
        ├── MusicPlayerException.java      # Abstract base
        ├── FileAccessException.java
        ├── MetadataException.java
        ├── PlaybackException.java
        ├── PlaylistException.java
        └── LibraryStorageException.java    # v2.0
```

### Usage Guidelines

| Layer | Exception Handling |
|-------|-------------------|
| **Infrastructure** | Catch infrastructure-specific exceptions, wrap in domain exceptions |
| **Application** | Catch domain exceptions, log, decide whether to propagate |
| **UI** | Catch domain exceptions, display user-friendly messages |

```java
// Infrastructure - wrap external exceptions
public class JAudioTaggerReader implements MetadataReader {
    @Override
    public TrackMetadata read(Path filePath) {
        try {
            // JAudioTagger exception handling
        } catch (CannotReadException e) {
            throw new MetadataException("Cannot read file: " + filePath, e);
        }
    }
}

// Application - log and handle
public class MetadataService {
    public Track loadTrack(Path path) {
        try {
            return reader.read(path);
        } catch (MetadataException e) {
            logger.warn("Failed to read metadata: {}", path, e);
            return Track.fromPath(path);  // Fallback with defaults
        }
    }
}
```

---

## 12. Configuration

### 12.1 Configuration File

Application configuration is stored in `config.properties` (loaded from classpath or current directory).

```
src/main/resources/
└── config.properties
```

### 12.2 Configuration Properties

```properties
# Music Player Configuration
# Version 1.0.0

# Audio Settings
audio.volume.default=0.7
audio.volume.muted=false

# Scanning Settings
scan.recursive=true
scan.sort.order=ALPHABETICAL

# Playback Settings
playback.auto-next=true
playback.repeat.default=OFF

# UI Settings
ui.show-notifications=true
ui.progress-update-ms=100
```

### 12.3 Configuration Access

```java
public class AppConfig {
    private static final Properties properties = new Properties();
    
    public static void load() {
        // Try classpath first, then current directory
        try (var input = Main.class.getResourceAsStream("/config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        }
    }
    
    public static double getVolumeDefault() {
        return Double.parseDouble(properties.getProperty("audio.volume.default", "0.7"));
    }
    
    public static boolean isRecursiveScanning() {
        return Boolean.parseBoolean(properties.getProperty("scan.recursive", "true"));
    }
}
```

### 12.4 Configuration Order of Precedence

| Priority | Source | Description |
|----------|--------|-------------|
| 1 | System properties | `-Dconfig.file=/path/to/config.properties` |
| 2 | Current directory | `./config.properties` |
| 3 | User home | `~/.musicplayer/config.properties` |
| 4 | Classpath | `/config.properties` (defaults) |

---

## 13. Test Setup

### 13.1 Test Data Location

```
src/test/
├── java/
│   └── com/musicplayer/
│       └── ...
│
└── resources/
    ├── test-data/
    │   ├── valid-track.mp3           # Valid MP3 with metadata
    │   ├── track-no-metadata.mp3     # Valid MP3 without tags
    │   ├── track-long.mp3             # Long track (5+ min)
    │   └── corrupted-file.mp3         # Corrupted/truncated file
    │
    └── test-config.properties         # Test-specific config
```

### 13.2 Obtaining Test Data

**Important:** Do NOT commit actual MP3 files to the repository due to copyright.

**Options for Test Data:**

| Option | Description | Setup |
|--------|-------------|-------|
| **Generate synthetic** | Create minimal valid MP3 programmatically | Complex, not recommended |
| **Public domain tracks** | Use tracks from Free Music Archive, ccMixter | Download and include |
| **Download at test time** | Fetch from public URL during test | Requires network |
| **Use existing files** | Reference files from user's system | Not portable |

**Recommended:** Include 2-3 short (30 seconds) public domain MP3 files in `src/test/resources/test-data/`.

### 13.3 Test Data File Naming

```
<description>-<scenario>.<ext>

Examples:
├── valid-track-complete.mp3       # Has all metadata
├── valid-track-minimal.mp3       # Only has title
├── valid-track-no-metadata.mp3   # No ID3 tags at all
├── corrupted-truncated.mp3       # File cut off mid-frame
├── corrupted-wrong-header.mp3    # Invalid MP3 header
└── valid-track-short.mp3         # < 30 seconds for fast tests
```

### 13.4 Mocking Strategy

For unit tests, mock the `AudioPlayer` interface:

```java
class AudioServiceTest {
    @Mock
    private AudioPlayer mockPlayer;
    
    @Test
    void shouldPlayTrackAndNotifyListener() {
        // Given
        when(mockPlayer.isPlaying()).thenReturn(true);
        
        // When
        audioService.play(testTrack);
        
        // Then
        verify(mockPlayer).play(testTrack);
        assertThat(audioService.isPlaying()).isTrue();
    }
}
```

### 13.5 Test Categories

| Category | Location | Purpose |
|----------|----------|---------|
| **Unit Tests** | `src/test/java/.../*Test.java` | Test individual classes |
| **Integration Tests** | `src/test/java/.../*IT.java` | Test component interactions |
| **Mock Tests** | Same as unit | Use Mockito for dependencies |

---

## 14. Build & Packaging

### 14.1 Maven Configuration

```xml
<!-- pom.xml additions -->
<plugin>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-maven-plugin</artifactId>
    <version>0.0.8</version>
    <configuration>
        <mainClass>com.musicplayer.Main</mainClass>
    </configuration>
</plugin>

<!-- Assembly for fat JAR -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.5.1</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <transformers>
                    <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                        <mainClass>com.musicplayer.Main</mainClass>
                    </transformer>
                </transformers>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 14.2 JavaFX Module Requirements

Java 21 requires explicit module access for JavaFX:

**Maven Command:**
```bash
mvn javafx:run
```

**Direct Run:**
```bash
java --add-modules javafx.controls,javafx.media \
     -cp target/music-player-1.0.0.jar \
     com.musicplayer.Main
```

### 14.3 Platform-Specific Packaging

#### macOS (.app bundle)

Use `jpackage` with macOS options:

```bash
jpackage \
    --type app-image \
    --input target/ \
    --main-jar music-player-1.0.0.jar \
    --name "Music Player" \
    --mac-package-identifier com.musicplayer.app \
    --mac-package-name "Music Player" \
    --icon resources/icon.icns
```

#### macOS App Structure

```
Music Player.app/
├── Contents/
│   ├── MacOS/
│   │   └── Music Player          # Launcher script
│   ├── Resources/
│   │   └── music-player-1.0.0.jar
│   └── Info.plist
```

#### macOS-Specific Considerations

| Item | Note |
|------|------|
| **Code Signing** | Required for distribution, optional for development |
| **Notarization** | Required for distribution outside Mac App Store |
| **.dmg creation** | Use `jpackage --type dmg` or create manually with hdiutil |
| **ARM/Intel** | Universal binary recommended (--mac-sign) |
| **Java Version** | Bundle JRE or require system Java 21+ |

#### Linux (.deb, .rpm, AppImage)

```bash
# Debian package
jpackage --type deb \
    --name music-player \
    --input target/ \
    --main-jar music-player-1.0.0.jar \
    --linux-deb-maintainer "user@email.com"

# RPM package
jpackage --type rpm \
    --name music-player \
    --input target/ \
    --main-jar music-player-1.0.0.jar
```

#### Windows (.exe, .msi)

```bash
jpackage --type exe \
    --name "Music Player" \
    --input target/ \
    --main-jar music-player-1.0.0.jar \
    --win-console
```

### 14.4 Build Commands Summary

| Command | Output | Platform |
|---------|--------|----------|
| `mvn package` | JAR file | All |
| `mvn javafx:run` | Run directly | All |
| `jpackage --type app-image` | .app folder | macOS |
| `jpackage --type dmg` | .dmg installer | macOS |
| `jpackage --type deb` | .deb package | Linux |
| `jpackage --type exe` | .exe installer | Windows |

### 14.5 Release Artifact

```
releases/
├── Music-Player-1.0.0-mac.dmg          # macOS installer
├── Music-Player-1.0.0-linux.deb        # Debian/Ubuntu
├── Music-Player-1.0.0-linux.rpm       # Fedora/RHEL
├── Music-Player-1.0.0-windows.exe      # Windows installer
└── Music-Player-1.0.0.jar             # Portable JAR
```

---

## 16. Implementation Order

| Phase | Tasks | Requirements |
|-------|-------|--------------|
| **Phase 1** | Domain models, Contracts, Infrastructure implementations | REQ-001, REQ-005, REQ-008, REQ-009 |
| **Phase 2** | Application services, PlayerController | REQ-002, REQ-006 |
| **Phase 3** | JavaFX UI implementation | REQ-003, REQ-004, REQ-007 |
| **Phase 4** | Error handling refinement, testing | All v1.0 |
| **Phase 5** | Domain extensions (LibraryRepository, artwork) | REQ-011, REQ-012 |
| **Phase 6** | Infrastructure persistence (JsonLibraryRepository) | REQ-011 |
| **Phase 7** | Application (LibraryService, PlayerController integration) | REQ-011 |
| **Phase 8** | UI layout redesign (BorderPane, QueuePanel, BrowserPanel) | REQ-010 |
| **Phase 9** | Artwork display (ArtworkPanel) | REQ-012 |
| **Phase 10** | Testing and refinement | All v2.0 |

---

## 17. Quality Requirements

| Requirement | Target |
|-------------|--------|
| **Code Coverage** | >80% for domain and application layers |
| **Compilation** | Zero warnings, zero errors |
| **Test Execution** | All tests pass on every build |
| **Startup Time** | <2 seconds to empty state |
| **Memory Usage** | <200MB baseline |

---

## 18. References

- **Requirements Document:** `../requirements/requirements.md`
- **Design Patterns:** Gang of Four (GoF)
- **Clean Architecture:** Robert C. Martin
- **SOLID Principles:** Robert C. Martin

---

## 19. Change Log

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-04-01 | Initial architecture document |
| 1.0.1 | 2026-04-01 | Added exception hierarchy, configuration, test setup, and build/packaging sections |
| 2.0.0 | 2026-04-01 | Added v2.0 architecture: Library persistence (REQ-011), Album art (REQ-012), New layout (REQ-010) |
