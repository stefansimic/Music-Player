package com.musicplayer.infrastructure.audio;

import com.musicplayer.domain.contract.AudioPlayer;
import com.musicplayer.domain.exception.PlaybackException;
import com.musicplayer.domain.model.Track;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * JavaFX-based implementation of the AudioPlayer contract.
 * 
 * Uses JavaFX MediaPlayer for audio playback with position tracking.
 */
public class JavaFxAudioPlayer implements AudioPlayer {
    
    private static final Logger logger = LoggerFactory.getLogger(JavaFxAudioPlayer.class);
    private static final Duration POSITION_UPDATE_INTERVAL = Duration.ofMillis(100);
    
    private MediaPlayer mediaPlayer;
    private Track currentTrack;
    private double volume = 0.7;
    private boolean isPlaying = false;
    private final CopyOnWriteArrayList<PlaybackListener> listeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> positionUpdateTask;

    @Override
    public void play(Track track) throws PlaybackException {
        validateTrack(track);
        
        try {
            stopCurrentPlayback();
            
            currentTrack = track;
            String path = track.getPath();
            
            java.net.URI uri = java.nio.file.Paths.get(path).toUri();
            String mediaUri = uri.toString();
            
            Media media = new Media(mediaUri);
            mediaPlayer = new MediaPlayer(media);
            
            mediaPlayer.setVolume(volume);
            mediaPlayer.setOnPlaying(this::onPlaying);
            mediaPlayer.setOnPaused(this::onPaused);
            mediaPlayer.setOnStopped(this::onStopped);
            mediaPlayer.setOnEndOfMedia(this::onEndOfMedia);
            mediaPlayer.setOnError(this::onMediaError);
            
            mediaPlayer.play();
            isPlaying = true;
            startPositionUpdates();
            
            logger.info("Started playback: {}", track.getTitle());
        } catch (Exception e) {
            logger.error("Failed to play track: {}", track.getPath(), e);
            throw new PlaybackException("Failed to play track: " + track.getTitle(), e);
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            stopPositionUpdates();
            notifyPlaybackStateChanged(false);
            logger.debug("Playback paused");
        }
    }

    @Override
    public void resume() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.play();
            isPlaying = true;
            startPositionUpdates();
            notifyPlaybackStateChanged(true);
            logger.debug("Playback resumed");
        }
    }

    @Override
    public void stop() {
        stopCurrentPlayback();
        isPlaying = false;
        currentTrack = null;
        notifyPlaybackStateChanged(false);
        logger.debug("Playback stopped");
    }

    @Override
    public void seek(Duration position) {
        if (mediaPlayer != null) {
            long milliseconds = position.toMillis();
            mediaPlayer.seek(javafx.util.Duration.millis(milliseconds));
            logger.debug("Seeked to position: {}", position);
        }
    }

    @Override
    public void setVolume(double volume) {
        this.volume = Math.max(0.0, Math.min(1.0, volume));
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(this.volume);
        }
        logger.debug("Volume set to: {}", this.volume);
    }

    @Override
    public double getVolume() {
        return volume;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public Duration getCurrentPosition() {
        if (mediaPlayer == null) {
            return Duration.ZERO;
        }
        long millis = (long) mediaPlayer.getCurrentTime().toMillis();
        return Duration.ofMillis(millis);
    }

    @Override
    public Duration getDuration() {
        if (mediaPlayer == null || currentTrack == null) {
            return Duration.ZERO;
        }
        Duration trackDuration = currentTrack.getDuration();
        if (trackDuration != null && !trackDuration.isZero()) {
            return trackDuration;
        }
        javafx.util.Duration fxDuration = mediaPlayer.getTotalDuration();
        if (fxDuration == null || fxDuration.isUnknown() || fxDuration.lessThanOrEqualTo(javafx.util.Duration.ZERO)) {
            return Duration.ZERO;
        }
        long millis = (long) fxDuration.toMillis();
        return Duration.ofMillis(millis);
    }

    @Override
    public void addPlaybackListener(PlaybackListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @Override
    public void removePlaybackListener(PlaybackListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void dispose() {
        stop();
        stopPositionUpdates();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        listeners.clear();
        logger.info("AudioPlayer disposed");
    }

    private void validateTrack(Track track) {
        if (track == null || track.getPath() == null) {
            throw new IllegalArgumentException("Track cannot be null or have null path");
        }
    }

    private void stopCurrentPlayback() {
        stopPositionUpdates();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    private void startPositionUpdates() {
        stopPositionUpdates();
        positionUpdateTask = scheduler.scheduleAtFixedRate(
            this::updatePosition,
            POSITION_UPDATE_INTERVAL.toMillis(),
            POSITION_UPDATE_INTERVAL.toMillis(),
            TimeUnit.MILLISECONDS
        );
    }

    private void stopPositionUpdates() {
        if (positionUpdateTask != null) {
            positionUpdateTask.cancel(false);
            positionUpdateTask = null;
        }
    }

    private void updatePosition() {
        if (mediaPlayer != null && isPlaying) {
            Duration position = getCurrentPosition();
            Duration duration = getDuration();
            notifyPositionChanged(position, duration);
        }
    }

    private void onPlaying() {
        isPlaying = true;
        Platform.runLater(() -> notifyPlaybackStateChanged(true));
    }

    private void onPaused() {
        isPlaying = false;
        Platform.runLater(() -> notifyPlaybackStateChanged(false));
    }

    private void onStopped() {
        isPlaying = false;
        Platform.runLater(() -> notifyPlaybackStateChanged(false));
    }

    private void onEndOfMedia() {
        isPlaying = false;
        stopPositionUpdates();
        Platform.runLater(() -> notifyTrackFinished());
    }

    private void onMediaError() {
        final String errorMessage;
        if (mediaPlayer != null && mediaPlayer.getError() != null) {
            errorMessage = mediaPlayer.getError().getMessage();
        } else {
            errorMessage = "Media playback error";
        }
        logger.error("Media error: {}", errorMessage);
        Platform.runLater(() -> notifyError(errorMessage));
    }

    private void notifyPlaybackStateChanged(boolean playing) {
        for (PlaybackListener listener : listeners) {
            try {
                listener.onPlaybackStateChanged(playing);
            } catch (Exception e) {
                logger.error("Error notifying listener", e);
            }
        }
    }

    private void notifyPositionChanged(Duration position, Duration duration) {
        for (PlaybackListener listener : listeners) {
            try {
                listener.onPositionChanged(position, duration);
            } catch (Exception e) {
                logger.error("Error notifying position listener", e);
            }
        }
    }

    private void notifyTrackFinished() {
        for (PlaybackListener listener : listeners) {
            try {
                listener.onTrackFinished();
            } catch (Exception e) {
                logger.error("Error notifying track finished", e);
            }
        }
    }

    private void notifyError(String error) {
        for (PlaybackListener listener : listeners) {
            try {
                listener.onError(error);
            } catch (Exception e) {
                logger.error("Error notifying error", e);
            }
        }
    }
}
