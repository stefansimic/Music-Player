package com.musicplayer.domain.exception;

/**
 * Exception thrown when a playlist operation fails.
 * 
 * This includes operations on empty playlists or invalid indices.
 */
public class PlaylistException extends MusicPlayerException {

    /**
     * Creates a new exception with a message.
     *
     * @param message the error message
     */
    public PlaylistException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with a message and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public PlaylistException(String message, Throwable cause) {
        super(message, cause);
    }
}
