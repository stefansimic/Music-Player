package com.musicplayer.domain.exception;

/**
 * Exception thrown when metadata cannot be read from a file.
 * 
 * This includes corrupted files, missing tags, or format errors.
 */
public class MetadataException extends MusicPlayerException {

    /**
     * Creates a new exception with a message.
     *
     * @param message the error message
     */
    public MetadataException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with a message and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public MetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}
