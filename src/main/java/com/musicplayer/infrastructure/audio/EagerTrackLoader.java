package com.musicplayer.infrastructure.audio;

import com.musicplayer.domain.contract.AudioPlayer;
import com.musicplayer.domain.contract.TrackLoader;
import com.musicplayer.domain.exception.PlaybackException;
import com.musicplayer.domain.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Standard implementation of TrackLoader that loads all tracks eagerly.
 * 
 * This is the traditional behavior where tracks are loaded on-demand
 * but no special resource management is performed.
 */
public class EagerTrackLoader implements TrackLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(EagerTrackLoader.class);
    
    private final AudioPlayer audioPlayer;
    private final CopyOnWriteArrayList<TrackLoadListener> listeners;
    
    private List<Track> availableTracks;
    private boolean resourceSavingMode;

    public EagerTrackLoader(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.listeners = new CopyOnWriteArrayList<>();
        this.availableTracks = new ArrayList<>();
        this.resourceSavingMode = false;
    }

    @Override
    public void initialize(List<Track> tracks) {
        this.availableTracks = new ArrayList<>(tracks);
        logger.info("EagerTrackLoader initialized with {} tracks", tracks.size());
    }

    @Override
    public void loadTrack(Track track) throws PlaybackException {
        if (track == null) return;
        audioPlayer.play(track);
        logger.debug("Loaded track: {}", track.getTitle());
    }

    @Override
    public List<Track> getPreloadedTracks() {
        return new ArrayList<>(availableTracks);
    }

    @Override
    public boolean isTrackLoaded(Track track) {
        return track != null;
    }

    @Override
    public void unloadAll() {
        logger.debug("UnloadAll called - no-op in EagerTrackLoader");
    }

    @Override
    public Track advancePreload(int currentIndex, int playlistSize) {
        return null;
    }

    @Override
    public Track rewindPreload(int targetIndex) {
        return null;
    }

    @Override
    public void addLoadListener(TrackLoadListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeLoadListener(TrackLoadListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean isResourceSavingMode() {
        return false;
    }

    @Override
    public void setResourceSavingMode(boolean enabled) {
        this.resourceSavingMode = enabled;
        logger.info("Resource-saving mode set to {} (no effect in EagerTrackLoader)", enabled);
    }

    public void dispose() {
        listeners.clear();
        logger.info("EagerTrackLoader disposed");
    }
}
