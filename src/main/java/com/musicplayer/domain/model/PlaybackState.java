package com.musicplayer.domain.model;

/**
 * Represents the current playback state of the player.
 */
public enum PlaybackState {
    
    /** No track is loaded or selected */
    IDLE,
    
    /** Track is currently playing */
    PLAYING,
    
    /** Playback is paused */
    PAUSED,
    
    /** Playback has stopped */
    STOPPED
}
