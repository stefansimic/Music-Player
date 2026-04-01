package com.musicplayer.infrastructure;

import com.musicplayer.infrastructure.filesystem.NioFileScanner;
import com.musicplayer.domain.exception.FileAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NioFileScannerTest {

    private NioFileScanner scanner;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        scanner = new NioFileScanner();
    }

    @Test
    void shouldFindMp3FilesInDirectory() throws IOException {
        createMp3File("song1.mp3");
        createMp3File("song2.mp3");
        createFile("readme.txt");

        var paths = scanner.scanDirectory(tempDir, false);

        assertThat(paths).hasSize(2);
        assertThat(paths).allMatch(p -> p.toString().endsWith(".mp3"));
    }

    @Test
    void shouldIgnoreNonMp3Files() throws IOException {
        createMp3File("song.mp3");
        createFile("document.pdf");
        createFile("image.jpg");
        createFile("video.mp4");

        var paths = scanner.scanDirectory(tempDir, false);

        assertThat(paths).hasSize(1);
    }

    @Test
    void shouldScanRecursively() throws IOException {
        createMp3File("song1.mp3");
        Path subDir = Files.createDirectory(tempDir.resolve("subdir"));
        createMp3File(subDir, "song2.mp3");

        var paths = scanner.scanDirectory(tempDir, true);

        assertThat(paths).hasSize(2);
    }

    @Test
    void shouldNotScanSubdirectoriesWhenRecursiveIsFalse() throws IOException {
        createMp3File("song1.mp3");
        Path subDir = Files.createDirectory(tempDir.resolve("subdir"));
        createMp3File(subDir, "song2.mp3");

        var paths = scanner.scanDirectory(tempDir, false);

        assertThat(paths).hasSize(1);
    }

    @Test
    void shouldReturnEmptyListForEmptyDirectory() {
        var paths = scanner.scanDirectory(tempDir, false);

        assertThat(paths).isEmpty();
    }

    @Test
    void shouldSortFilesAlphabetically() throws IOException {
        createMp3File("zebra.mp3");
        createMp3File("apple.mp3");
        createMp3File("mango.mp3");

        var paths = scanner.scanAndSort(tempDir, false);

        assertThat(paths).hasSize(3);
        assertThat(paths.get(0).getFileName().toString()).isEqualTo("apple.mp3");
        assertThat(paths.get(1).getFileName().toString()).isEqualTo("mango.mp3");
        assertThat(paths.get(2).getFileName().toString()).isEqualTo("zebra.mp3");
    }

    @Test
    void shouldSortCaseInsensitive() throws IOException {
        createMp3File("Zebra.mp3");
        createMp3File("apple.mp3");
        createMp3File("Mango.mp3");

        var paths = scanner.scanAndSort(tempDir, false);

        assertThat(paths).hasSize(3);
        assertThat(paths.get(0).getFileName().toString()).isEqualTo("apple.mp3");
    }

    @Test
    void shouldSortAlphabetically() throws IOException {
        createMp3File("track10.mp3");
        createMp3File("track2.mp3");
        createMp3File("track1.mp3");

        var paths = scanner.scanAndSort(tempDir, false);

        assertThat(paths).hasSize(3);
        assertThat(paths.get(0).getFileName().toString()).isEqualTo("track1.mp3");
        assertThat(paths.get(1).getFileName().toString()).isEqualTo("track10.mp3");
        assertThat(paths.get(2).getFileName().toString()).isEqualTo("track2.mp3");
    }

    @Test
    void shouldRejectNullDirectory() {
        assertThrows(FileAccessException.class, () -> scanner.scanDirectory(null, false));
    }

    @Test
    void shouldRejectNonExistentDirectory() {
        Path nonExistent = tempDir.resolve("does-not-exist");

        assertThrows(FileAccessException.class, () -> scanner.scanDirectory(nonExistent, false));
    }

    @Test
    void shouldRejectFileInsteadOfDirectory() throws IOException {
        Path file = createFile("regular-file.txt");

        assertThrows(FileAccessException.class, () -> scanner.scanDirectory(file, false));
    }

    @Test
    void shouldHandleDeepNestedDirectories() throws IOException {
        createMp3File("song1.mp3");
        Path level1 = Files.createDirectory(tempDir.resolve("level1"));
        Path level2 = Files.createDirectory(level1.resolve("level2"));
        Path level3 = Files.createDirectory(level2.resolve("level3"));
        createMp3File(level3, "deep-song.mp3");

        var paths = scanner.scanDirectory(tempDir, true);

        assertThat(paths).hasSize(2);
    }

    private void createMp3File(String name) throws IOException {
        createFile(name);
    }

    private void createMp3File(Path dir, String name) throws IOException {
        Files.createFile(dir.resolve(name));
    }

    private Path createFile(String name) throws IOException {
        return Files.createFile(tempDir.resolve(name));
    }
}
