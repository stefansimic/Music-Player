package com.musicplayer.domain.model;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the music library containing all imported tracks and their metadata.
 * 
 * The library persists between application sessions and tracks file changes
 * to enable incremental rescanning.
 */
public class Library {
    
    private final List<Path> importPaths;
    private final List<LibraryEntry> entries;
    private long lastScanTimestamp;

    /**
     * Creates an empty library.
     */
    public Library() {
        this.importPaths = new ArrayList<>();
        this.entries = new ArrayList<>();
        this.lastScanTimestamp = 0;
    }

    /**
     * Creates a library with the specified import paths and entries.
     *
     * @param importPaths     the directories to monitor for music
     * @param entries         the cached track entries
     * @param lastScanTimestamp the timestamp of the last scan
     */
    public Library(List<Path> importPaths, List<LibraryEntry> entries, long lastScanTimestamp) {
        this.importPaths = new ArrayList<>(Objects.requireNonNullElse(importPaths, Collections.emptyList()));
        this.entries = new ArrayList<>(Objects.requireNonNullElse(entries, Collections.emptyList()));
        this.lastScanTimestamp = lastScanTimestamp;
    }

    /**
     * Record representing a single entry in the library.
     *
     * @param path        the file path to the track
     * @param title       the track title
     * @param artist      the artist name
     * @param album       the album name
     * @param duration    the track duration
     * @param artwork     the album artwork bytes (may be null)
     * @param lastModified the file's last modification timestamp
     */
    public record LibraryEntry(
            Path path,
            String title,
            String artist,
            String album,
            Duration duration,
            byte[] artwork,
            long lastModified
    ) {
        
        /**
         * Creates an entry without artwork.
         */
        public LibraryEntry(Path path, String title, String artist, String album, Duration duration, long lastModified) {
            this(path, title, artist, album, duration, null, lastModified);
        }
        
        /**
         * Returns the title, or "Unknown" if empty.
         */
        public String title() {
            return (title != null && !title.isBlank()) ? title : "Unknown";
        }
        
        /**
         * Returns the artist, or "Unknown" if empty.
         */
        public String artist() {
            return (artist != null && !artist.isBlank()) ? artist : "Unknown";
        }
        
        /**
         * Returns the album, or "Unknown" if empty.
         */
        public String album() {
            return (album != null && !album.isBlank()) ? album : "Unknown";
        }
        
        /**
         * Returns whether this entry has artwork.
         */
        public boolean hasArtwork() {
            return artwork != null && artwork.length > 0;
        }
    }

    /**
     * Adds an import path to the library.
     *
     * @param path the directory path to add
     */
    public void addImportPath(Path path) {
        if (path != null && !importPaths.contains(path)) {
            importPaths.add(path);
        }
    }

    /**
     * Removes an import path from the library.
     *
     * @param path the directory path to remove
     * @return true if the path was removed
     */
    public boolean removeImportPath(Path path) {
        return importPaths.remove(path);
    }

    /**
     * Returns the list of import paths.
     *
     * @return an unmodifiable view of the import paths
     */
    public List<Path> getImportPaths() {
        return Collections.unmodifiableList(importPaths);
    }

    /**
     * Adds an entry to the library.
     *
     * @param entry the library entry to add
     */
    public void addEntry(LibraryEntry entry) {
        if (entry != null) {
            entries.add(entry);
        }
    }

    /**
     * Adds multiple entries to the library.
     *
     * @param newEntries the entries to add
     */
    public void addEntries(List<LibraryEntry> newEntries) {
        if (newEntries != null) {
            entries.addAll(newEntries);
        }
    }

    /**
     * Removes an entry from the library by path.
     *
     * @param path the path to remove
     * @return true if an entry was removed
     */
    public boolean removeEntry(Path path) {
        return entries.removeIf(e -> e.path().equals(path));
    }

    /**
     * Returns the list of library entries.
     *
     * @return an unmodifiable view of the entries
     */
    public List<LibraryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Returns the number of entries in the library.
     *
     * @return the entry count
     */
    public int getEntryCount() {
        return entries.size();
    }

    /**
     * Sets the last scan timestamp.
     *
     * @param timestamp the timestamp in milliseconds
     */
    public void setLastScanTimestamp(long timestamp) {
        this.lastScanTimestamp = timestamp;
    }

    /**
     * Returns the last scan timestamp.
     *
     * @return the timestamp in milliseconds
     */
    public long getLastScanTimestamp() {
        return lastScanTimestamp;
    }

    /**
     * Clears all entries from the library.
     */
    public void clearEntries() {
        entries.clear();
    }

    /**
     * Clears all import paths from the library.
     */
    public void clearImportPaths() {
        importPaths.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Library library = (Library) o;
        return lastScanTimestamp == library.lastScanTimestamp
                && Objects.equals(importPaths, library.importPaths)
                && Objects.equals(entries, library.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(importPaths, entries, lastScanTimestamp);
    }

    @Override
    public String toString() {
        return String.format("Library{importPaths=%d, entries=%d, lastScanTimestamp=%d}",
                importPaths.size(), entries.size(), lastScanTimestamp);
    }
}
