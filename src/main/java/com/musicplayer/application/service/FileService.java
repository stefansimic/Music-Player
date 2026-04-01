package com.musicplayer.application.service;

import com.musicplayer.domain.contract.FileScanner;
import com.musicplayer.domain.contract.MetadataReader;
import com.musicplayer.domain.exception.FileAccessException;
import com.musicplayer.domain.exception.MetadataException;
import com.musicplayer.domain.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for loading and managing tracks from the file system.
 * 
 * Handles directory scanning, metadata reading, and track creation.
 */
public class FileService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    
    private final FileScanner fileScanner;
    private final MetadataReader metadataReader;
    private final ExecutorService executor;
    private final boolean recursive;
    
    public FileService(FileScanner fileScanner, MetadataReader metadataReader, boolean recursive) {
        this.fileScanner = fileScanner;
        this.metadataReader = metadataReader;
        this.recursive = recursive;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Scans a directory and creates Track objects with metadata.
     *
     * @param directory the directory to scan
     * @param progressCallback callback for scan progress (may be null)
     * @return list of discovered tracks
     * @throws FileAccessException if directory cannot be accessed
     */
    public List<Track> loadDirectory(Path directory, ProgressCallback progressCallback) 
            throws FileAccessException {
        logger.info("Scanning directory: {} (recursive: {})", directory, recursive);
        
        List<Path> paths = fileScanner.scanAndSort(directory, recursive);
        
        if (paths.isEmpty()) {
            logger.info("No MP3 files found in: {}", directory);
            return List.of();
        }
        
        logger.info("Found {} MP3 files", paths.size());
        List<Track> tracks = new ArrayList<>();
        
        for (int i = 0; i < paths.size(); i++) {
            Path path = paths.get(i);
            Track track = createTrackFromPath(path);
            tracks.add(track);
            
            if (progressCallback != null) {
                progressCallback.onProgress(i + 1, paths.size(), path.getFileName().toString());
            }
        }
        
        return tracks;
    }

    /**
     * Asynchronously loads tracks from a directory.
     *
     * @param directory the directory to scan
     * @param callback callback with the loaded tracks
     */
    public void loadDirectoryAsync(Path directory, LoadCallback callback) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return loadDirectory(directory, null);
            } catch (FileAccessException e) {
                throw new RuntimeException(e);
            }
        }, executor).thenAccept(callback::onLoaded)
                   .exceptionally(e -> {
                       callback.onError(e.getCause().getMessage());
                       return null;
                   });
    }

    private Track createTrackFromPath(Path path) {
        try {
            MetadataReader.TrackMetadata metadata = metadataReader.read(path);
            return new Track(
                path.toString(),
                metadata.title(),
                metadata.artist(),
                metadata.album(),
                metadata.duration()
            );
        } catch (MetadataException e) {
            logger.warn("Failed to read metadata for: {}, using defaults", path);
            return new Track(path.toString());
        }
    }

    public void shutdown() {
        executor.shutdown();
    }

    @FunctionalInterface
    public interface ProgressCallback {
        void onProgress(int current, int total, String currentFile);
    }

    public interface LoadCallback {
        default void onLoaded(List<Track> tracks) {}
        default void onError(String error) {}
    }
}
