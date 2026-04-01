package com.musicplayer.domain.model;

/**
 * Represents the repeat mode for playlist playback.
 * 
 * Defines how the player behaves when reaching the end of the playlist.
 */
public enum RepeatMode {
    
    /**
     * Playback stops after the last track finishes.
     */
    OFF,
    
    /**
     * Restart from the first track after the last track finishes.
     */
    ALL,
    
    /**
     * Repeat the current track continuously.
     */
    ONE;

    /**
     * Returns the next repeat mode in the cycle: OFF -> ALL -> ONE -> OFF.
     *
     * @return the next repeat mode
     */
    public RepeatMode next() {
        return switch (this) {
            case OFF -> ALL;
            case ALL -> ONE;
            case ONE -> OFF;
        };
    }
}
