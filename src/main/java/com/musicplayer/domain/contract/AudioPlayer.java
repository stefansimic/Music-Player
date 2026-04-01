package com.musicplayer.domain.contract;

import com.musicplayer.domain.exception.PlaybackException;
import com.musicplayer.domain.model.Track;

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

/**
 * Contract for audio playback functionality.
 * 
 * Implementations provide actual audio playback using various backends
 * (e.g., JavaFX MediaPlayer).
 */
public interface AudioPlayer {
    
    /**
     * Listener interface for playback state changes.
     */
    interface PlaybackListener {
        
        /**
         * Called when playback state changes.
         *
         * @param isPlaying true if playing, false if paused/stopped
         */
        void onPlaybackStateChanged(boolean isPlaying);
        
        /**
         * Called when the current position updates.
         *
         * @param position the current position
         * @param duration the total duration
         */
        void onPositionChanged(Duration position, Duration duration);
        
        /**
         * Called when a track finishes playing.
         */
        void onTrackFinished();
        
        /**
         * Called when an error occurs during playback.
         *
         * @param error the error message
         */
        void onError(String error);
    }

    /**
     * Starts playback of the specified track.
     *
     * @param track the track to play
     * @throws PlaybackException if playback cannot be started
     */
    void play(Track track) throws PlaybackException;

    /**
     * Pauses current playback.
     */
    void pause();

    /**
     * Resumes paused playback.
     */
    void resume();

    /**
     * Stops playback and resets position to beginning.
     */
    void stop();

    /**
     * Seeks to the specified position.
     *
     * @param position the target position
     */
    void seek(Duration position);

    /**
     * Sets the playback volume.
     *
     * @param volume volume level from 0.0 (muted) to 1.0 (max)
     */
    void setVolume(double volume);

    /**
     * Returns the current volume level.
     *
     * @return volume from 0.0 to 1.0
     */
    double getVolume();

    /**
     * Returns whether playback is currently active.
     *
     * @return true if playing
     */
    boolean isPlaying();

    /**
     * Returns the current playback position.
     *
     * @return current position
     */
    Duration getCurrentPosition();

    /**
     * Returns the duration of the current track.
     *
     * @return duration, or null if no track loaded
     */
    Duration getDuration();

    /**
     * Adds a listener for playback events.
     *
     * @param listener the listener to add
     */
    void addPlaybackListener(PlaybackListener listener);

    /**
     * Removes a playback listener.
     *
     * @param listener the listener to remove
     */
    void removePlaybackListener(PlaybackListener listener);

    /**
     * Releases all resources held by the player.
     * Should be called when the player is no longer needed.
     */
    void dispose();
}
