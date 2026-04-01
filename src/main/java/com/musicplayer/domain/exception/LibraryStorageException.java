package com.musicplayer.domain.exception;

/**
 * Exception thrown when library storage operations fail.
 */
public class LibraryStorageException extends MusicPlayerException {

    /**
     * Creates a new exception with a message.
     *
     * @param message the error message
     */
    public LibraryStorageException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with a message and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public LibraryStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
