package com.musicplayer.domain.contract;

import com.musicplayer.domain.exception.MetadataException;

import java.nio.file.Path;
import java.time.Duration;

/**
 * Contract for reading metadata from audio files.
 */
public interface MetadataReader {

    /**
     * Record representing metadata extracted from an audio file.
     *
     * @param title    the track title
     * @param artist   the artist name
     * @param album    the album name
     * @param duration the track duration
     */
    record TrackMetadata(String title, String artist, String album, Duration duration) {
        
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
    }

    /**
     * Reads metadata from an audio file.
     *
     * @param filePath the path to the audio file
     * @return the extracted metadata
     * @throws MetadataException if metadata cannot be read
     */
    TrackMetadata read(Path filePath) throws MetadataException;
}
