package com.musicplayer.application.service;

import com.musicplayer.domain.contract.AudioPlayer;
import com.musicplayer.domain.exception.PlaybackException;
import com.musicplayer.domain.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service for managing audio playback.
 * 
 * Wraps the AudioPlayer implementation and provides a clean API
 * for the controller layer.
 */
public class AudioService {
    
    private static final Logger logger = LoggerFactory.getLogger(AudioService.class);
    
    private final AudioPlayer audioPlayer;
    private final CopyOnWriteArrayList<PlaybackListener> listeners = new CopyOnWriteArrayList<>();
    
    public AudioService(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.audioPlayer.addPlaybackListener(createListenerAdapter());
    }

    private AudioPlayer.PlaybackListener createListenerAdapter() {
        return new AudioPlayer.PlaybackListener() {
            @Override
            public void onPlaybackStateChanged(boolean isPlaying) {
                notifyPlaybackStateChanged(isPlaying);
            }

            @Override
            public void onPositionChanged(Duration position, Duration duration) {
                notifyPositionChanged(position, duration);
            }

            @Override
            public void onTrackFinished() {
                notifyTrackFinished();
            }

            @Override
            public void onError(String error) {
                notifyError(error);
            }
        };
    }

    public void play(Track track) throws PlaybackException {
        audioPlayer.play(track);
    }

    public void pause() {
        audioPlayer.pause();
    }

    public void resume() {
        audioPlayer.resume();
    }

    public void stop() {
        audioPlayer.stop();
    }

    public void seek(Duration position) {
        audioPlayer.seek(position);
    }

    public void setVolume(double volume) {
        audioPlayer.setVolume(volume);
    }

    public double getVolume() {
        return audioPlayer.getVolume();
    }

    public boolean isPlaying() {
        return audioPlayer.isPlaying();
    }

    public Duration getCurrentPosition() {
        return audioPlayer.getCurrentPosition();
    }

    public Duration getDuration() {
        return audioPlayer.getDuration();
    }

    public void addPlaybackListener(PlaybackListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removePlaybackListener(PlaybackListener listener) {
        listeners.remove(listener);
    }

    public void dispose() {
        audioPlayer.dispose();
        listeners.clear();
    }

    private void notifyPlaybackStateChanged(boolean isPlaying) {
        for (PlaybackListener listener : listeners) {
            try {
                listener.onPlaybackStateChanged(isPlaying);
            } catch (Exception e) {
                logger.error("Error in playback state listener", e);
            }
        }
    }

    private void notifyPositionChanged(Duration position, Duration duration) {
        for (PlaybackListener listener : listeners) {
            try {
                listener.onPositionChanged(position, duration);
            } catch (Exception e) {
                logger.error("Error in position listener", e);
            }
        }
    }

    private void notifyTrackFinished() {
        for (PlaybackListener listener : listeners) {
            try {
                listener.onTrackFinished();
            } catch (Exception e) {
                logger.error("Error in track finished listener", e);
            }
        }
    }

    private void notifyError(String error) {
        for (PlaybackListener listener : listeners) {
            try {
                listener.onError(error);
            } catch (Exception e) {
                logger.error("Error in error listener", e);
            }
        }
    }

    public interface PlaybackListener {
        void onPlaybackStateChanged(boolean isPlaying);
        void onPositionChanged(Duration position, Duration duration);
        void onTrackFinished();
        void onError(String error);
    }
}
