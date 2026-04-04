package com.musicplayer.domain.contract;

import com.musicplayer.domain.exception.PlaybackException;
import com.musicplayer.domain.model.Track;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Strategy interface for loading and managing audio tracks.
 * 
 * Implementations can provide different loading strategies:
 * - Eager loading: All tracks are fully loaded in memory
 * - Lazy loading: Only essential metadata is kept, tracks are loaded on-demand
 */
public interface TrackLoader {

    /**
     * Listener interface for track loading events.
     */
    interface TrackLoadListener {
        void onTrackPreloaded(Track track);
        void onTrackUnloaded(Track track);
        void onPreloadQueueChanged(List<Track> preloadedTracks);
        void onLoadingError(Track track, String error);
    }

    /**
     * Initializes the track loader with the list of available tracks.
     *
     * @param tracks the list of tracks available for playback
     */
    void initialize(List<Track> tracks);

    /**
     * Loads a track for playback, potentially preloading nearby tracks.
     *
     * @param track the track to load
     * @throws PlaybackException if loading fails
     */
    void loadTrack(Track track) throws PlaybackException;

    /**
     * Returns the currently loaded (preloaded) tracks.
     *
     * @return list of preloaded tracks
     */
    List<Track> getPreloadedTracks();

    /**
     * Returns whether a specific track is currently loaded.
     *
     * @param track the track to check
     * @return true if track is loaded
     */
    boolean isTrackLoaded(Track track);

    /**
     * Unloads all preloaded tracks and releases resources.
     */
    void unloadAll();

    /**
     * Advances to the next track position and manages preload queue.
     * Returns the track that should be preloaded next.
     *
     * @param currentIndex the current track index
     * @param playlistSize the total size of the playlist
     * @return the track to preload, or null if none
     */
    Track advancePreload(int currentIndex, int playlistSize);

    /**
     * Goes back to a previous track position and manages preload queue.
     * Returns the track that should be preloaded.
     *
     * @param targetIndex the target track index to load
     * @return the track to preload, or null if none
     */
    Track rewindPreload(int targetIndex);

    /**
     * Adds a listener for loading events.
     *
     * @param listener the listener to add
     */
    void addLoadListener(TrackLoadListener listener);

    /**
     * Removes a loading listener.
     *
     * @param listener the listener to remove
     */
    void removeLoadListener(TrackLoadListener listener);

    /**
     * Returns whether resource-saving mode is enabled.
     *
     * @return true if lazy/resource-saving mode
     */
    boolean isResourceSavingMode();

    /**
     * Sets the resource-saving mode.
     *
     * @param enabled true to enable resource-saving mode
     */
    void setResourceSavingMode(boolean enabled);
}
