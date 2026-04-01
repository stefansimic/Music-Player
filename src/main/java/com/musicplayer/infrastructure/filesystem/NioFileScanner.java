package com.musicplayer.infrastructure.filesystem;

import com.musicplayer.domain.contract.FileScanner;
import com.musicplayer.domain.exception.FileAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * NIO-based implementation of the FileScanner contract.
 * 
 * Uses Java NIO for efficient recursive directory scanning with sorting.
 */
public class NioFileScanner implements FileScanner {
    
    private static final Logger logger = LoggerFactory.getLogger(NioFileScanner.class);
    private static final String MP3_EXTENSION = ".mp3";

    @Override
    public List<Path> scanDirectory(Path directory, boolean recursive) throws FileAccessException {
        validateDirectory(directory);
        
        try (Stream<Path> pathStream = buildPathStream(directory, recursive)) {
            return pathStream
                .filter(this::isMp3File)
                .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Failed to scan directory: {}", directory, e);
            throw new FileAccessException("Failed to scan directory: " + directory, e);
        }
    }

    @Override
    public List<Path> scanAndSort(Path directory, boolean recursive) throws FileAccessException {
        validateDirectory(directory);
        
        try (Stream<Path> pathStream = buildPathStream(directory, recursive)) {
            return pathStream
                .filter(this::isMp3File)
                .sorted(createPathComparator())
                .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Failed to scan and sort directory: {}", directory, e);
            throw new FileAccessException("Failed to scan and sort directory: " + directory, e);
        }
    }

    private void validateDirectory(Path directory) throws FileAccessException {
        if (directory == null) {
            throw new FileAccessException("Directory cannot be null");
        }
        if (!Files.exists(directory)) {
            throw new FileAccessException("Directory does not exist: " + directory);
        }
        if (!Files.isDirectory(directory)) {
            throw new FileAccessException("Path is not a directory: " + directory);
        }
        if (!Files.isReadable(directory)) {
            throw new FileAccessException("Directory is not readable: " + directory);
        }
    }

    private Stream<Path> buildPathStream(Path directory, boolean recursive) throws IOException {
        if (recursive) {
            return Files.walk(directory);
        } else {
            return Files.list(directory);
        }
    }

    private boolean isMp3File(Path path) {
        if (path == null) {
            return false;
        }
        String fileName = path.getFileName() != null 
            ? path.getFileName().toString().toLowerCase() 
            : "";
        return fileName.endsWith(MP3_EXTENSION);
    }

    private Comparator<Path> createPathComparator() {
        return (p1, p2) -> {
            String name1 = getSortableName(p1);
            String name2 = getSortableName(p2);
            return name1.compareToIgnoreCase(name2);
        };
    }

    private String getSortableName(Path path) {
        if (path == null || path.getFileName() == null) {
            return "";
        }
        return path.getFileName().toString();
    }
}
