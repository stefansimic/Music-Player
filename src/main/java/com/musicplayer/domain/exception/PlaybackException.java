package com.musicplayer.domain.exception;

/**
 * Exception thrown when audio playback fails.
 * 
 * This includes playback initialization errors, buffer underruns,
 * or unsupported formats.
 */
public class PlaybackException extends MusicPlayerException {

    /**
     * Creates a new exception with a message.
     *
     * @param message the error message
     */
    public PlaybackException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with a message and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public PlaybackException(String message, Throwable cause) {
        super(message, cause);
    }
}
