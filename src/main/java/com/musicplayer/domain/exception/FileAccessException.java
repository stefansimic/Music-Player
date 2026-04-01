package com.musicplayer.domain.exception;

/**
 * Exception thrown when a file cannot be accessed.
 * 
 * This includes file not found, permission denied, or I/O errors.
 */
public class FileAccessException extends MusicPlayerException {

    /**
     * Creates a new exception with a message.
     *
     * @param message the error message
     */
    public FileAccessException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with a message and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public FileAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
