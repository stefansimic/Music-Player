package com.musicplayer.domain.contract;

import com.musicplayer.domain.exception.FileAccessException;

import java.nio.file.Path;
import java.util.List;

/**
 * Contract for scanning directories for audio files.
 */
public interface FileScanner {

    /**
     * Scans a directory for MP3 files.
     *
     * @param directory the directory to scan
     * @param recursive if true, scans subdirectories as well
     * @return list of paths to MP3 files
     * @throws FileAccessException if directory cannot be accessed
     */
    List<Path> scanDirectory(Path directory, boolean recursive) throws FileAccessException;

    /**
     * Scans a directory and returns files sorted alphabetically.
     *
     * @param directory the directory to scan
     * @param recursive if true, scans subdirectories as well
     * @return sorted list of paths to MP3 files
     * @throws FileAccessException if directory cannot be accessed
     */
    List<Path> scanAndSort(Path directory, boolean recursive) throws FileAccessException;
}
