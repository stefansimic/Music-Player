package com.musicplayer.domain.exception;

/**
 * Base exception class for all music player exceptions.
 * 
 * Domain exceptions have no dependencies on infrastructure libraries,
 * ensuring clean separation of layers.
 */
public abstract class MusicPlayerException extends RuntimeException {

    /**
     * Creates a new exception with a message.
     *
     * @param message the error message
     */
    protected MusicPlayerException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with a message and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    protected MusicPlayerException(String message, Throwable cause) {
        super(message, cause);
    }
}
