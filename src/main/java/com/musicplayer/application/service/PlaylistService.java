package com.musicplayer.application.service;

import com.musicplayer.domain.model.PlaybackState;
import com.musicplayer.domain.model.Playlist;
import com.musicplayer.domain.model.RepeatMode;
import com.musicplayer.domain.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service for managing the playlist and playback state.
 * 
 * Handles track queue, navigation, shuffle, and repeat modes.
 */
public class PlaylistService {
    
    private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);
    
    private final Playlist playlist;
    private final CopyOnWriteArrayList<PlaylistListener> listeners = new CopyOnWriteArrayList<>();
    
    public PlaylistService() {
        this.playlist = new Playlist("Main Playlist");
    }

    public PlaylistService(Playlist playlist) {
        this.playlist = playlist;
    }

    public void setTracks(List<Track> tracks) {
        playlist.clear();
        for (Track track : tracks) {
            playlist.addTrack(track);
        }
        notifyPlaylistChanged();
        logger.info("Playlist loaded with {} tracks", tracks.size());
    }

    public void addTrack(Track track) {
        playlist.addTrack(track);
        notifyPlaylistChanged();
    }

    public void removeTrack(int index) {
        playlist.removeTrack(index);
        notifyPlaylistChanged();
    }

    public Track getCurrentTrack() {
        return playlist.getCurrentTrack();
    }

    public Track getTrack(int index) {
        return playlist.getTrack(index);
    }

    public int getCurrentIndex() {
        return playlist.getCurrentIndex();
    }

    public void setCurrentIndex(int index) {
        playlist.setCurrentIndex(index);
        notifyTrackChanged();
    }

    public Track next() {
        Track track = playlist.getNextTrack();
        notifyTrackChanged();
        return track;
    }

    public Track previous() {
        Track track = playlist.getPreviousTrack();
        notifyTrackChanged();
        return track;
    }

    public void toggleShuffle() {
        if (playlist.isShuffled()) {
            playlist.unshuffle();
            logger.info("Shuffle disabled");
        } else {
            playlist.shuffle();
            logger.info("Shuffle enabled");
        }
        notifyPlaylistChanged();
    }

    public boolean isShuffled() {
        return playlist.isShuffled();
    }

    public RepeatMode nextRepeatMode() {
        RepeatMode newMode = playlist.nextRepeatMode();
        logger.info("Repeat mode changed to: {}", newMode);
        notifyRepeatModeChanged();
        return newMode;
    }

    public RepeatMode getRepeatMode() {
        return playlist.getRepeatMode();
    }

    public void setRepeatMode(RepeatMode mode) {
        playlist.setRepeatMode(mode);
        notifyRepeatModeChanged();
    }

    public List<Track> getTracks() {
        return playlist.getTracks();
    }

    public int getSize() {
        return playlist.size();
    }

    public boolean isEmpty() {
        return playlist.isEmpty();
    }

    public void clear() {
        playlist.clear();
        notifyPlaylistChanged();
    }

    public void addListener(PlaylistListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(PlaylistListener listener) {
        listeners.remove(listener);
    }

    private void notifyTrackChanged() {
        for (PlaylistListener listener : listeners) {
            try {
                listener.onTrackChanged(getCurrentTrack(), getCurrentIndex());
            } catch (Exception e) {
                logger.error("Error notifying track change", e);
            }
        }
    }

    private void notifyPlaylistChanged() {
        for (PlaylistListener listener : listeners) {
            try {
                listener.onPlaylistChanged(getTracks());
            } catch (Exception e) {
                logger.error("Error notifying playlist change", e);
            }
        }
    }

    private void notifyRepeatModeChanged() {
        for (PlaylistListener listener : listeners) {
            try {
                listener.onRepeatModeChanged(getRepeatMode(), isShuffled());
            } catch (Exception e) {
                logger.error("Error notifying repeat mode change", e);
            }
        }
    }

    public interface PlaylistListener {
        void onTrackChanged(Track track, int index);
        void onPlaylistChanged(List<Track> playlist);
        void onRepeatModeChanged(RepeatMode repeatMode, boolean shuffled);
    }
}
