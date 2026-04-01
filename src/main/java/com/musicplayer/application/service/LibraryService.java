package com.musicplayer.application.service;

import com.musicplayer.domain.contract.FileScanner;
import com.musicplayer.domain.contract.LibraryRepository;
import com.musicplayer.domain.contract.MetadataReader;
import com.musicplayer.domain.exception.LibraryStorageException;
import com.musicplayer.domain.exception.MetadataException;
import com.musicplayer.domain.model.Library;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Service for managing the music library persistence and rescanning.
 */
public class LibraryService {
    
    private static final Logger logger = LoggerFactory.getLogger(LibraryService.class);
    
    private final LibraryRepository repository;
    private final FileScanner fileScanner;
    private final MetadataReader metadataReader;
    private final ExecutorService executor;
    
    private Library library;

    /**
     * Creates a new LibraryService.
     *
     * @param repository      the library repository for persistence
     * @param fileScanner     the file scanner for discovering MP3 files
     * @param metadataReader  the metadata reader for extracting track info
     */
    public LibraryService(LibraryRepository repository, FileScanner fileScanner, MetadataReader metadataReader) {
        this.repository = repository;
        this.fileScanner = fileScanner;
        this.metadataReader = metadataReader;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Loads the library from storage.
     *
     * @return the loaded library, or a new empty library if none exists
     */
    public Library loadLibrary() {
        try {
            library = repository.load();
            logger.info("Library loaded with {} tracks from {} paths", 
                    library.getEntryCount(), library.getImportPaths().size());
        } catch (LibraryStorageException e) {
            logger.warn("Failed to load library, creating new one: {}", e.getMessage());
            library = new Library();
        }
        return library;
    }

    /**
     * Saves the library to storage.
     */
    public void saveLibrary() {
        try {
            library.setLastScanTimestamp(System.currentTimeMillis());
            repository.save(library);
            logger.info("Library saved successfully");
        } catch (LibraryStorageException e) {
            logger.error("Failed to save library: {}", e.getMessage(), e);
        }
    }

    /**
     * Returns the current library.
     *
     * @return the library
     */
    public Library getLibrary() {
        if (library == null) {
            return loadLibrary();
        }
        return library;
    }

    /**
     * Adds an import path and scans it.
     *
     * @param importPath the directory path to import
     * @param progressCallback callback for scan progress (current, total)
     * @return completable future that completes with the number of new tracks
     */
    public CompletableFuture<Integer> addImportPath(Path importPath, Consumer<ScanProgress> progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            if (library == null) {
                loadLibrary();
            }
            
            library.addImportPath(importPath);
            
            List<Path> existingPaths = library.getEntries().stream()
                    .map(Library.LibraryEntry::path)
                    .toList();
            
            List<Path> allFiles = fileScanner.scanAndSort(importPath, true);
            List<Path> newFiles = allFiles.stream()
                    .filter(p -> !existingPaths.contains(p))
                    .toList();
            
            int total = newFiles.size();
            int current = 0;
            int added = 0;
            
            for (Path filePath : newFiles) {
                current++;
                
                if (progressCallback != null) {
                    progressCallback.accept(new ScanProgress(current, total, filePath.getFileName().toString()));
                }
                
                try {
                    if (Files.exists(filePath)) {
                        Library.LibraryEntry entry = createEntry(filePath);
                        if (entry != null) {
                            library.addEntry(entry);
                            added++;
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Failed to process file {}: {}", filePath, e.getMessage());
                }
            }
            
            saveLibrary();
            logger.info("Added {} new tracks from {}", added, importPath);
            
            return added;
        }, executor);
    }

    /**
     * Removes an import path and its tracks from the library.
     *
     * @param importPath the directory path to remove
     */
    public void removeImportPath(Path importPath) {
        if (library == null) {
            return;
        }
        
        library.getEntries().stream()
                .filter(e -> e.path().startsWith(importPath))
                .map(Library.LibraryEntry::path)
                .toList()
                .forEach(library::removeEntry);
        
        library.removeImportPath(importPath);
        saveLibrary();
        logger.info("Removed import path and tracks from library: {}", importPath);
    }

    /**
     * Performs an incremental rescan of all import paths.
     * Only rescans files that have changed since the last scan.
     *
     * @param progressCallback callback for scan progress
     * @return completable future that completes with the number of changed tracks
     */
    public CompletableFuture<RescanResult> rescanLibrary(Consumer<ScanProgress> progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            if (library == null) {
                loadLibrary();
            }
            
            RescanResult result = new RescanResult();
            
            Map<Path, Library.LibraryEntry> entryMap = new HashMap<>();
            library.getEntries().forEach(e -> entryMap.put(e.path(), e));
            
            List<Path> currentPaths = new ArrayList<>();
            
            for (Path importPath : library.getImportPaths()) {
                List<Path> files = fileScanner.scanAndSort(importPath, true);
                currentPaths.addAll(files);
            }
            
            List<Path> trackedPaths = new ArrayList<>(entryMap.keySet());
            
            for (Path path : trackedPaths) {
                if (!currentPaths.contains(path)) {
                    library.removeEntry(path);
                    result.removed++;
                }
            }
            
            int total = currentPaths.size();
            int current = 0;
            
            for (Path filePath : currentPaths) {
                current++;
                
                if (progressCallback != null) {
                    progressCallback.accept(new ScanProgress(current, total, filePath.getFileName().toString()));
                }
                
                try {
                    Library.LibraryEntry existing = entryMap.get(filePath);
                    long currentModified = Files.exists(filePath) 
                            ? Files.getLastModifiedTime(filePath).toMillis() 
                            : 0;
                    
                    if (existing == null) {
                        Library.LibraryEntry entry = createEntry(filePath);
                        if (entry != null) {
                            library.addEntry(entry);
                            result.added++;
                        }
                    } else if (currentModified > existing.lastModified()) {
                        library.removeEntry(filePath);
                        Library.LibraryEntry entry = createEntry(filePath);
                        if (entry != null) {
                            library.addEntry(entry);
                            result.updated++;
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Failed to process file {}: {}", filePath, e.getMessage());
                }
            }
            
            saveLibrary();
            logger.info("Rescan complete: {} added, {} updated, {} removed", 
                    result.added, result.updated, result.removed);
            
            return result;
        }, executor);
    }

    /**
     * Forces a full rescan of all import paths.
     *
     * @param progressCallback callback for scan progress
     * @return completable future that completes with the number of scanned tracks
     */
    public CompletableFuture<Integer> forceFullRescan(Consumer<ScanProgress> progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            if (library == null) {
                loadLibrary();
            }
            
            library.clearEntries();
            
            int total = library.getImportPaths().size();
            int current = 0;
            int totalTracks = 0;
            
            for (Path importPath : library.getImportPaths()) {
                current++;
                
                List<Path> files = fileScanner.scanAndSort(importPath, true);
                
                for (Path filePath : files) {
                    if (progressCallback != null) {
                        progressCallback.accept(new ScanProgress(
                                totalTracks + 1, -1, filePath.getFileName().toString()));
                    }
                    
                    try {
                        Library.LibraryEntry entry = createEntry(filePath);
                        if (entry != null) {
                            library.addEntry(entry);
                            totalTracks++;
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to process file {}: {}", filePath, e.getMessage());
                    }
                }
            }
            
            saveLibrary();
            logger.info("Full rescan complete: {} tracks", totalTracks);
            
            return totalTracks;
        }, executor);
    }

    private Library.LibraryEntry createEntry(Path filePath) {
        try {
            MetadataReader.TrackMetadata metadata = metadataReader.read(filePath);
            long lastModified = getLastModifiedTime(filePath);
            
            return new Library.LibraryEntry(
                    filePath,
                    metadata.title(),
                    metadata.artist(),
                    metadata.album(),
                    metadata.duration(),
                    metadata.artwork(),
                    lastModified
            );
        } catch (MetadataException e) {
            logger.debug("Failed to read metadata for {}: {}", filePath, e.getMessage());
            return new Library.LibraryEntry(
                    filePath,
                    filePath.getFileName().toString(),
                    "Unknown",
                    "Unknown",
                    null,
                    null,
                    getLastModifiedTime(filePath)
            );
        }
    }
    
    private long getLastModifiedTime(Path filePath) {
        try {
            return Files.exists(filePath) ? Files.getLastModifiedTime(filePath).toMillis() : 0;
        } catch (java.io.IOException e) {
            logger.debug("Failed to get last modified time for {}: {}", filePath, e.getMessage());
            return 0;
        }
    }

    /**
     * Shuts down the executor service.
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Progress information for scan operations.
     */
    public record ScanProgress(int current, int total, String currentFile) {
        public double getPercentage() {
            return total > 0 ? (double) current / total * 100 : -1;
        }
    }

    /**
     * Result of a rescan operation.
     */
    public static class RescanResult {
        public int added = 0;
        public int updated = 0;
        public int removed = 0;
        
        public int getTotalChanged() {
            return added + updated + removed;
        }
    }
}
