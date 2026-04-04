package com.musicplayer.infrastructure.audio;

import com.musicplayer.domain.contract.AudioPlayer;
import com.musicplayer.domain.contract.TrackLoader;
import com.musicplayer.domain.exception.PlaybackException;
import com.musicplayer.domain.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Resource-saving implementation of TrackLoader.
 * 
 * Only preloads the next N tracks (configurable, default 5) in a LinkedQueue.
 * When advancing, the oldest preloaded track is evicted and the next one is loaded.
 * This significantly reduces memory usage for large playlists.
 */
public class LazyTrackLoader implements TrackLoader, AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(LazyTrackLoader.class);
    private static final int DEFAULT_PRELOAD_COUNT = 5;
    
    private final AudioPlayer audioPlayer;
    private final ExecutorService preloadExecutor;
    private final int preloadCount;
    
    private final ConcurrentLinkedQueue<Track> preloadQueue;
    private final Set<String> loadedPaths;
    private final CopyOnWriteArrayList<TrackLoadListener> listeners;
    
    private List<Track> availableTracks;
    private boolean resourceSavingMode;
    private volatile boolean disposed;

    public LazyTrackLoader(AudioPlayer audioPlayer) {
        this(audioPlayer, DEFAULT_PRELOAD_COUNT);
    }
    
    public LazyTrackLoader(AudioPlayer audioPlayer, int preloadCount) {
        this.audioPlayer = audioPlayer;
        this.preloadCount = preloadCount;
        this.preloadQueue = new ConcurrentLinkedQueue<>();
        this.loadedPaths = Collections.synchronizedSet(new HashSet<>());
        this.listeners = new CopyOnWriteArrayList<>();
        this.availableTracks = new ArrayList<>();
        this.resourceSavingMode = true;
        this.preloadExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "TrackPreload");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void initialize(List<Track> tracks) {
        if (disposed) return;
        this.availableTracks = new ArrayList<>(tracks);
        unloadAll();
        logger.info("LazyTrackLoader initialized with {} tracks, preloading {}",
                tracks.size(), preloadCount);
    }

    @Override
    public void loadTrack(Track track) throws PlaybackException {
        if (disposed || track == null) return;
        
        if (!resourceSavingMode) {
            audioPlayer.play(track);
            return;
        }
        
        int trackIndex = findTrackIndex(track);
        preloadForPosition(trackIndex);
        audioPlayer.play(track);
        logger.debug("Loaded track: {}", track.getTitle());
    }

    @Override
    public List<Track> getPreloadedTracks() {
        return new ArrayList<>(preloadQueue);
    }

    @Override
    public boolean isTrackLoaded(Track track) {
        return track != null && loadedPaths.contains(track.getPath());
    }

    @Override
    public void unloadAll() {
        preloadQueue.clear();
        loadedPaths.clear();
        logger.debug("All preloaded tracks unloaded");
        notifyQueueChanged();
    }

    @Override
    public Track advancePreload(int currentIndex, int playlistSize) {
        if (!resourceSavingMode || disposed) return null;
        
        int windowStart = currentIndex + 1;
        int windowEnd = Math.min(playlistSize, windowStart + preloadCount);
        
        if (preloadQueue.size() >= preloadCount) {
            Track evicted = preloadQueue.poll();
            if (evicted != null) {
                loadedPaths.remove(evicted.getPath());
                notifyTrackUnloaded(evicted);
                logger.debug("Evicted from preload queue: {}", evicted.getTitle());
            }
        }
        
        for (int i = windowStart; i < windowEnd; i++) {
            Track track = availableTracks.get(i);
            if (!loadedPaths.contains(track.getPath())) {
                loadedPaths.add(track.getPath());
                preloadQueue.offer(track);
                notifyTrackPreloaded(track);
            }
        }
        
        notifyQueueChanged();
        
        return windowStart < playlistSize ? availableTracks.get(windowStart) : null;
    }

    @Override
    public Track rewindPreload(int targetIndex) {
        if (!resourceSavingMode || disposed) return null;
        
        unloadAll();
        
        int startIndex = Math.max(0, targetIndex);
        int endIndex = Math.min(availableTracks.size(), targetIndex + preloadCount);
        
        for (int i = startIndex; i < endIndex; i++) {
            Track track = availableTracks.get(i);
            preloadQueue.offer(track);
            loadedPaths.add(track.getPath());
        }
        
        notifyQueueChanged();
        logger.debug("Rewound preload: {} tracks preloaded starting from index {}", 
                preloadQueue.size(), startIndex);
        
        return targetIndex < availableTracks.size() ? availableTracks.get(targetIndex) : null;
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
        return resourceSavingMode;
    }

    @Override
    public void setResourceSavingMode(boolean enabled) {
        this.resourceSavingMode = enabled;
        if (!enabled) {
            unloadAll();
        }
        logger.info("Resource-saving mode: {}", enabled);
    }

    public void dispose() {
        close();
    }
    
    @Override
    public void close() {
        disposed = true;
        unloadAll();
        preloadExecutor.shutdown();
        listeners.clear();
        logger.info("LazyTrackLoader disposed");
    }
    
    private void preloadForPosition(int centerIndex) {
        unloadAll();
        
        if (availableTracks.isEmpty()) return;
        
        int startIndex = Math.max(0, centerIndex);
        int endIndex = Math.min(availableTracks.size(), centerIndex + preloadCount);
        
        for (int i = startIndex; i < endIndex; i++) {
            Track track = availableTracks.get(i);
            preloadQueue.offer(track);
            loadedPaths.add(track.getPath());
            notifyTrackPreloaded(track);
        }
        
        notifyQueueChanged();
        logger.debug("Preloaded {} tracks from index {}", preloadQueue.size(), startIndex);
    }
    
    private void preloadAsync(Track track) {
        if (track == null || loadedPaths.contains(track.getPath())) return;
        
        preloadExecutor.submit(() -> {
            try {
                loadedPaths.add(track.getPath());
                preloadQueue.offer(track);
                notifyTrackPreloaded(track);
                notifyQueueChanged();
                logger.debug("Async preloaded: {}", track.getTitle());
            } catch (Exception e) {
                logger.warn("Failed to preload track {}: {}", track.getTitle(), e.getMessage());
                loadedPaths.remove(track.getPath());
                notifyLoadingError(track, e.getMessage());
            }
        });
    }
    
    private int findTrackIndex(Track track) {
        if (track == null) return 0;
        for (int i = 0; i < availableTracks.size(); i++) {
            if (availableTracks.get(i).getPath().equals(track.getPath())) {
                return i;
            }
        }
        return 0;
    }
    
    private void notifyTrackPreloaded(Track track) {
        for (TrackLoadListener listener : listeners) {
            try {
                listener.onTrackPreloaded(track);
            } catch (Exception e) {
                logger.error("Error in TrackLoadListener.onTrackPreloaded", e);
            }
        }
    }
    
    private void notifyTrackUnloaded(Track track) {
        for (TrackLoadListener listener : listeners) {
            try {
                listener.onTrackUnloaded(track);
            } catch (Exception e) {
                logger.error("Error in TrackLoadListener.onTrackUnloaded", e);
            }
        }
    }
    
    private void notifyQueueChanged() {
        for (TrackLoadListener listener : listeners) {
            try {
                listener.onPreloadQueueChanged(getPreloadedTracks());
            } catch (Exception e) {
                logger.error("Error in TrackLoadListener.onPreloadQueueChanged", e);
            }
        }
    }
    
    private void notifyLoadingError(Track track, String error) {
        for (TrackLoadListener listener : listeners) {
            try {
                listener.onLoadingError(track, error);
            } catch (Exception e) {
                logger.error("Error in TrackLoadListener.onLoadingError", e);
            }
        }
    }
}
