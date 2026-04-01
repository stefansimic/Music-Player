package com.musicplayer.domain.model;

import java.time.Duration;
import java.util.Objects;

/**
 * Represents a single audio track in the music player.
 * 
 * A track contains a file path and optional metadata such as title,
 * artist, album, and duration. The file path is immutable once set.
 */
public class Track {
    
    private final String path;
    private String title;
    private String artist;
    private String album;
    private Duration duration;
    private byte[] artwork;

    /**
     * Creates a new track from a file path only.
     * Title is extracted from the filename, artist and album default to "Unknown".
     *
     * @param path the absolute file path to the MP3 file
     * @throws IllegalArgumentException if path is null or empty
     */
    public Track(String path) {
        this.path = validatePath(path);
        this.title = extractTitleFromPath(path);
        this.artist = "Unknown";
        this.album = "Unknown";
    }

    /**
     * Creates a new track with full metadata.
     *
     * @param path     the absolute file path to the MP3 file
     * @param title    the track title (nullable, defaults to filename)
     * @param artist   the artist name (nullable, defaults to "Unknown")
     * @param album    the album name (nullable, defaults to "Unknown")
     * @param duration the track duration (nullable)
     * @throws IllegalArgumentException if path is null or empty
     */
    public Track(String path, String title, String artist, String album, Duration duration) {
        this(path, title, artist, album, duration, null);
    }
    
    /**
     * Creates a new track with full metadata including artwork.
     *
     * @param path     the absolute file path to the MP3 file
     * @param title    the track title (nullable, defaults to filename)
     * @param artist   the artist name (nullable, defaults to "Unknown")
     * @param album    the album name (nullable, defaults to "Unknown")
     * @param duration the track duration (nullable)
     * @param artwork  the album artwork bytes (nullable)
     * @throws IllegalArgumentException if path is null or empty
     */
    public Track(String path, String title, String artist, String album, Duration duration, byte[] artwork) {
        this.path = validatePath(path);
        this.title = (title != null && !title.isBlank()) ? title : extractTitleFromPath(path);
        this.artist = (artist != null && !artist.isBlank()) ? artist : "Unknown";
        this.album = (album != null && !album.isBlank()) ? album : "Unknown";
        this.duration = duration;
        this.artwork = artwork;
    }

    private String validatePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Track path cannot be null or empty");
        }
        return path;
    }

    private String extractTitleFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return "Unknown";
        }
        int lastSeparator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        String filename = (lastSeparator >= 0) ? path.substring(lastSeparator + 1) : path;
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex > 0) ? filename.substring(0, dotIndex) : filename;
    }

    /**
     * Returns the file path of this track.
     *
     * @return the absolute file path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the track title.
     *
     * @return the title, or "Unknown" if not available
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the track title.
     *
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = (title != null && !title.isBlank()) ? title : "Unknown";
    }

    /**
     * Returns the artist name.
     *
     * @return the artist, or "Unknown" if not available
     */
    public String getArtist() {
        return artist;
    }

    /**
     * Sets the artist name.
     *
     * @param artist the new artist name
     */
    public void setArtist(String artist) {
        this.artist = (artist != null && !artist.isBlank()) ? artist : "Unknown";
    }

    /**
     * Returns the album name.
     *
     * @return the album, or "Unknown" if not available
     */
    public String getAlbum() {
        return album;
    }

    /**
     * Sets the album name.
     *
     * @param album the new album name
     */
    public void setAlbum(String album) {
        this.album = (album != null && !album.isBlank()) ? album : "Unknown";
    }

    /**
     * Returns the track duration.
     *
     * @return the duration, or null if not available
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Sets the track duration.
     *
     * @param duration the new duration
     */
    public void setDuration(Duration duration) {
        this.duration = duration;
    }
    
    /**
     * Returns the album artwork.
     *
     * @return the artwork bytes, or null if not available
     */
    public byte[] getArtwork() {
        return artwork;
    }
    
    /**
     * Sets the album artwork.
     *
     * @param artwork the artwork bytes
     */
    public void setArtwork(byte[] artwork) {
        this.artwork = artwork;
    }
    
    /**
     * Returns whether this track has artwork.
     *
     * @return true if artwork is available
     */
    public boolean hasArtwork() {
        return artwork != null && artwork.length > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return Objects.equals(path, track.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return String.format("Track{title='%s', artist='%s', album='%s', duration=%s}", 
                title, artist, album, duration);
    }
}
