package com.musicplayer.application.controller;

import com.musicplayer.application.service.AudioService;
import com.musicplayer.application.service.FileService;
import com.musicplayer.application.service.LibraryService;
import com.musicplayer.application.service.PlaylistService;
import com.musicplayer.domain.exception.PlaybackException;
import com.musicplayer.domain.model.Library;
import com.musicplayer.domain.model.PlaybackState;
import com.musicplayer.domain.model.RepeatMode;
import com.musicplayer.domain.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Main controller (facade) for the music player.
 * 
 * Coordinates between AudioService, PlaylistService, FileService, and LibraryService
 * to provide a unified API for the UI layer.
 */
public class PlayerController {
    
    private static final Logger logger = LoggerFactory.getLogger(PlayerController.class);
    
    private final AudioService audioService;
    private final PlaylistService playlistService;
    private final FileService fileService;
    private final LibraryService libraryService;
    
    private final CopyOnWriteArrayList<PlayerStateListener> listeners = new CopyOnWriteArrayList<>();
    private volatile PlaybackState playbackState = PlaybackState.IDLE;
    
    public PlayerController(AudioService audioService, 
                           PlaylistService playlistService, 
                           FileService fileService,
                           LibraryService libraryService) {
        this.audioService = audioService;
        this.playlistService = playlistService;
        this.fileService = fileService;
        this.libraryService = libraryService;
        
        setupInternalListeners();
    }
    
    public PlayerController(AudioService audioService, 
                           PlaylistService playlistService, 
                           FileService fileService) {
        this(audioService, playlistService, fileService, null);
    }

    private void setupInternalListeners() {
        audioService.addPlaybackListener(new AudioService.PlaybackListener() {
            @Override
            public void onPlaybackStateChanged(boolean isPlaying) {
                updatePlaybackState(isPlaying ? PlaybackState.PLAYING : PlaybackState.PAUSED);
            }

            @Override
            public void onPositionChanged(Duration position, Duration duration) {
                notifyProgressChanged(position, duration);
            }

            @Override
            public void onTrackFinished() {
                handleTrackFinished();
            }

            @Override
            public void onError(String error) {
                notifyError("Playback error: " + error);
                handleTrackFinished();
            }
        });
        
        playlistService.addListener(new PlaylistService.PlaylistListener() {
            @Override
            public void onTrackChanged(Track track, int index) {
                notifyTrackChanged(track);
            }

            @Override
            public void onPlaylistChanged(List<Track> playlist) {
                notifyPlaylistChanged(playlist);
            }

            @Override
            public void onRepeatModeChanged(RepeatMode repeatMode, boolean shuffled) {
                notifyPlaybackModeChanged(repeatMode, shuffled);
            }
        });
    }

    public void loadDirectory(Path directory) {
        logger.info("Loading directory: {}", directory);
        try {
            List<Track> tracks = fileService.loadDirectory(directory, (current, total, file) -> {
                logger.debug("Scanning: {}/{} - {}", current, total, file);
            });
            
            if (tracks.isEmpty()) {
                notifyError("No MP3 files found in the selected directory");
                return;
            }
            
            playlistService.setTracks(tracks);
            logger.info("Loaded {} tracks", tracks.size());
            notifyInfo("Loaded " + tracks.size() + " tracks");
            
        } catch (Exception e) {
            logger.error("Failed to load directory", e);
            notifyError("Failed to load directory: " + e.getMessage());
        }
    }

    public void loadDirectoryAsync(Path directory) {
        logger.info("Loading directory asynchronously: {}", directory);
        fileService.loadDirectoryAsync(directory, new FileService.LoadCallback() {
            @Override
            public void onLoaded(List<Track> tracks) {
                if (tracks.isEmpty()) {
                    notifyError("No MP3 files found in the selected directory");
                    return;
                }
                playlistService.setTracks(tracks);
                notifyInfo("Loaded " + tracks.size() + " tracks");
            }

            @Override
            public void onError(String error) {
                notifyError("Failed to load directory: " + error);
            }
        });
    }

    public void play() {
        if (playlistService.isEmpty()) {
            notifyError("No tracks loaded");
            return;
        }
        
        Track currentTrack = playlistService.getCurrentTrack();
        if (currentTrack == null) {
            playlistService.setCurrentIndex(0);
            currentTrack = playlistService.getCurrentTrack();
        }
        
        try {
            audioService.play(currentTrack);
            updatePlaybackState(PlaybackState.PLAYING);
            logger.info("Playing: {}", currentTrack.getTitle());
        } catch (PlaybackException e) {
            logger.error("Failed to play track", e);
            notifyError("Failed to play: " + currentTrack.getTitle());
            handleTrackFinished();
        }
    }

    public void pause() {
        audioService.pause();
        updatePlaybackState(PlaybackState.PAUSED);
        logger.debug("Paused");
    }

    public void resume() {
        audioService.resume();
        updatePlaybackState(PlaybackState.PLAYING);
        logger.debug("Resumed");
    }

    public void stop() {
        audioService.stop();
        updatePlaybackState(PlaybackState.STOPPED);
        logger.info("Stopped");
    }

    public void togglePlayPause() {
        if (audioService.isPlaying()) {
            pause();
        } else if (playbackState == PlaybackState.PAUSED) {
            resume();
        } else {
            play();
        }
    }

    public void next() {
        Track track = playlistService.next();
        if (track != null && playbackState == PlaybackState.PLAYING) {
            try {
                audioService.play(track);
            } catch (PlaybackException e) {
                logger.error("Failed to play next track", e);
                notifyError("Failed to play: " + track.getTitle());
            }
        }
    }

    public void previous() {
        Duration position = audioService.getCurrentPosition();
        if (position != null && position.compareTo(Duration.ofSeconds(3)) > 0) {
            seek(Duration.ZERO);
            return;
        }
        
        Track track = playlistService.previous();
        if (track != null && playbackState == PlaybackState.PLAYING) {
            try {
                audioService.play(track);
            } catch (PlaybackException e) {
                logger.error("Failed to play previous track", e);
                notifyError("Failed to play: " + track.getTitle());
            }
        }
    }

    public void playTrack(int index) {
        if (index < 0 || index >= playlistService.getSize()) {
            logger.warn("Invalid track index: {}", index);
            return;
        }
        
        playlistService.setCurrentIndex(index);
        Track track = playlistService.getCurrentTrack();
        
        try {
            audioService.play(track);
            updatePlaybackState(PlaybackState.PLAYING);
        } catch (PlaybackException e) {
            logger.error("Failed to play track at index {}", index, e);
            notifyError("Failed to play: " + track.getTitle());
        }
    }

    public void seek(Duration position) {
        audioService.seek(position);
    }

    public void seekToPercentage(double percentage) {
        Duration duration = audioService.getDuration();
        if (duration != null && !duration.isZero()) {
            long newPosition = (long) (duration.toMillis() * percentage);
            seek(Duration.ofMillis(newPosition));
        }
    }

    public void setVolume(double volume) {
        audioService.setVolume(Math.max(0.0, Math.min(1.0, volume)));
        notifyVolumeChanged(audioService.getVolume());
    }

    public double getVolume() {
        return audioService.getVolume();
    }

    public void toggleShuffle() {
        playlistService.toggleShuffle();
    }

    public void toggleRepeat() {
        playlistService.nextRepeatMode();
    }

    public RepeatMode getRepeatMode() {
        return playlistService.getRepeatMode();
    }

    public boolean isShuffled() {
        return playlistService.isShuffled();
    }

    public Track getCurrentTrack() {
        return playlistService.getCurrentTrack();
    }

    public List<Track> getPlaylist() {
        return playlistService.getTracks();
    }
    
    public void setPlaylist(List<Track> tracks) {
        if (tracks != null && !tracks.isEmpty()) {
            playlistService.setTracks(tracks);
            notifyPlaylistChanged(tracks);
            logger.info("Playlist set with {} tracks", tracks.size());
        }
    }

    public int getCurrentIndex() {
        return playlistService.getCurrentIndex();
    }

    public PlaybackState getPlaybackState() {
        return playbackState;
    }

    public boolean isPlaying() {
        return playbackState == PlaybackState.PLAYING;
    }

    public Duration getCurrentPosition() {
        return audioService.getCurrentPosition();
    }

    public Duration getDuration() {
        return audioService.getDuration();
    }

    public void addStateListener(PlayerStateListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeStateListener(PlayerStateListener listener) {
        listeners.remove(listener);
    }

    public void initializeLibrary() {
        if (libraryService != null) {
            Library library = libraryService.loadLibrary();
            notifyLibraryLoaded(library);
            
            List<Track> tracks = convertLibraryToTracks(library);
            if (!tracks.isEmpty()) {
                playlistService.setTracks(tracks);
                notifyPlaylistChanged(tracks);
                notifyInfo("Loaded " + tracks.size() + " tracks from library");
            }
        }
    }
    
    public CompletableFuture<Integer> addImportPath(Path path, Consumer<LibraryService.ScanProgress> progressCallback) {
        if (libraryService == null) {
            return CompletableFuture.completedFuture(0);
        }
        
        return libraryService.addImportPath(path, (progress) -> {
            if (progressCallback != null) {
                progressCallback.accept(progress);
            }
        }).thenApply(added -> {
            Library library = libraryService.getLibrary();
            notifyLibraryChanged(library);
            
            List<Track> tracks = convertLibraryToTracks(library);
            playlistService.setTracks(tracks);
            notifyPlaylistChanged(tracks);
            
            return added;
        });
    }
    
    public void removeImportPath(Path path) {
        if (libraryService != null) {
            libraryService.removeImportPath(path);
            notifyLibraryChanged(libraryService.getLibrary());
            
            List<Track> tracks = convertLibraryToTracks(libraryService.getLibrary());
            playlistService.setTracks(tracks);
            notifyPlaylistChanged(tracks);
        }
    }
    
    public List<Path> getImportPaths() {
        if (libraryService == null) {
            return List.of();
        }
        return libraryService.getLibrary().getImportPaths();
    }
    
    public CompletableFuture<LibraryService.RescanResult> rescanLibrary(Consumer<LibraryService.ScanProgress> progressCallback) {
        if (libraryService == null) {
            return CompletableFuture.completedFuture(new LibraryService.RescanResult());
        }
        
        return libraryService.rescanLibrary((progress) -> {
            if (progressCallback != null) {
                progressCallback.accept(progress);
            }
        }).thenApply(result -> {
            Library library = libraryService.getLibrary();
            notifyLibraryChanged(library);
            
            List<Track> tracks = convertLibraryToTracks(library);
            playlistService.setTracks(tracks);
            notifyPlaylistChanged(tracks);
            
            notifyInfo("Rescan complete: " + result.added + " added, " + 
                    result.updated + " updated, " + result.removed + " removed");
            
            return result;
        });
    }
    
    public CompletableFuture<Integer> forceFullRescan(Consumer<LibraryService.ScanProgress> progressCallback) {
        if (libraryService == null) {
            return CompletableFuture.completedFuture(0);
        }
        
        return libraryService.forceFullRescan((progress) -> {
            if (progressCallback != null) {
                progressCallback.accept(progress);
            }
        }).thenApply(count -> {
            Library library = libraryService.getLibrary();
            notifyLibraryChanged(library);
            
            List<Track> tracks = convertLibraryToTracks(library);
            playlistService.setTracks(tracks);
            notifyPlaylistChanged(tracks);
            
            notifyInfo("Full rescan complete: " + count + " tracks");
            
            return count;
        });
    }
    
    public Library getLibrary() {
        if (libraryService == null) {
            return null;
        }
        return libraryService.getLibrary();
    }
    
    private List<Track> convertLibraryToTracks(Library library) {
        return library.getEntries().stream()
                .map(entry -> new Track(
                        entry.path().toString(),
                        entry.title(),
                        entry.artist(),
                        entry.album(),
                        entry.duration()
                ))
                .toList();
    }

    public void dispose() {
        audioService.dispose();
        fileService.shutdown();
        if (libraryService != null) {
            libraryService.saveLibrary();
            libraryService.shutdown();
        }
        listeners.clear();
        logger.info("PlayerController disposed");
    }

    private void handleTrackFinished() {
        RepeatMode repeatMode = playlistService.getRepeatMode();
        
        if (repeatMode == RepeatMode.ONE) {
            Track current = playlistService.getCurrentTrack();
            if (current != null) {
                try {
                    audioService.play(current);
                } catch (PlaybackException e) {
                    logger.error("Failed to replay track", e);
                }
            }
        } else {
            next();
        }
    }

    private void updatePlaybackState(PlaybackState newState) {
        if (this.playbackState != newState) {
            this.playbackState = newState;
            notifyPlaybackStateChanged(newState);
        }
    }

    private void notifyPlaybackStateChanged(PlaybackState state) {
        for (PlayerStateListener listener : listeners) {
            try {
                listener.onPlaybackStateChanged(state);
            } catch (Exception e) {
                logger.error("Error notifying state listener", e);
            }
        }
    }

    private void notifyTrackChanged(Track track) {
        for (PlayerStateListener listener : listeners) {
            try {
                listener.onTrackChanged(track);
            } catch (Exception e) {
                logger.error("Error notifying track listener", e);
            }
        }
    }

    private void notifyProgressChanged(Duration position, Duration duration) {
        for (PlayerStateListener listener : listeners) {
            try {
                listener.onProgressChanged(position, duration);
            } catch (Exception e) {
                logger.error("Error notifying progress listener", e);
            }
        }
    }

    private void notifyVolumeChanged(double volume) {
        for (PlayerStateListener listener : listeners) {
            try {
                listener.onVolumeChanged(volume);
            } catch (Exception e) {
                logger.error("Error notifying volume listener", e);
            }
        }
    }

    private void notifyPlaylistChanged(List<Track> playlist) {
        for (PlayerStateListener listener : listeners) {
            try {
                listener.onPlaylistChanged(playlist);
            } catch (Exception e) {
                logger.error("Error notifying playlist listener", e);
            }
        }
    }

    private void notifyPlaybackModeChanged(RepeatMode repeatMode, boolean shuffled) {
        for (PlayerStateListener listener : listeners) {
            try {
                listener.onPlaybackModeChanged(repeatMode, shuffled);
            } catch (Exception e) {
                logger.error("Error notifying mode listener", e);
            }
        }
    }

    private void notifyError(String message) {
        for (PlayerStateListener listener : listeners) {
            try {
                listener.onError(message);
            } catch (Exception e) {
                logger.error("Error notifying error listener", e);
            }
        }
    }

    private void notifyInfo(String message) {
        for (PlayerStateListener listener : listeners) {
            try {
                listener.onInfo(message);
            } catch (Exception e) {
                logger.error("Error notifying info listener", e);
            }
        }
    }
    
    private void notifyLibraryLoaded(Library library) {
        for (PlayerStateListener listener : listeners) {
            try {
                listener.onLibraryLoaded(library);
            } catch (Exception e) {
                logger.error("Error notifying library loaded listener", e);
            }
        }
    }
    
    private void notifyLibraryChanged(Library library) {
        for (PlayerStateListener listener : listeners) {
            try {
                listener.onLibraryChanged(library);
            } catch (Exception e) {
                logger.error("Error notifying library changed listener", e);
            }
        }
    }

    public interface PlayerStateListener {
        void onPlaybackStateChanged(PlaybackState state);
        void onTrackChanged(Track track);
        void onProgressChanged(Duration position, Duration duration);
        void onVolumeChanged(double volume);
        void onPlaylistChanged(List<Track> playlist);
        void onPlaybackModeChanged(RepeatMode repeatMode, boolean shuffled);
        void onError(String message);
        void onInfo(String message);
        void onLibraryLoaded(Library library);
        void onLibraryChanged(Library library);
    }
}
