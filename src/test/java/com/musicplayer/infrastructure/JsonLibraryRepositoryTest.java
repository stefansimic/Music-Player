package com.musicplayer.infrastructure;

import com.musicplayer.domain.contract.LibraryRepository;
import com.musicplayer.domain.exception.LibraryStorageException;
import com.musicplayer.domain.model.Library;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonLibraryRepositoryTest {

    @TempDir
    Path tempDir;

    private LibraryRepository repository;

    @BeforeEach
    void setUp() {
        Path libraryFile = tempDir.resolve("library.json");
        repository = new com.musicplayer.infrastructure.persistence.JsonLibraryRepository(libraryFile);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
    }

    @Test
    void shouldReturnFalseWhenLibraryDoesNotExist() {
        assertThat(repository.exists()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenLibraryExists() {
        Library library = createLibraryWithOneEntry();
        repository.save(library);

        assertThat(repository.exists()).isTrue();
    }

    @Test
    void shouldSaveAndLoadEmptyLibrary() {
        Library library = new Library();
        library.addImportPath(Paths.get("/music"));

        repository.save(library);
        Library loaded = repository.load();

        assertThat(loaded.getImportPaths()).containsExactly(Paths.get("/music"));
        assertThat(loaded.getEntries()).isEmpty();
    }

    @Test
    void shouldSaveAndLoadLibraryWithEntries() {
        Library library = createLibraryWithOneEntry();

        repository.save(library);
        Library loaded = repository.load();

        assertThat(loaded.getEntries()).hasSize(1);
        assertThat(loaded.getEntries().get(0).title()).isEqualTo("Test Song");
        assertThat(loaded.getEntries().get(0).artist()).isEqualTo("Test Artist");
        assertThat(loaded.getEntries().get(0).album()).isEqualTo("Test Album");
    }

    @Test
    void shouldSaveAndLoadLibraryWithArtwork() {
        byte[] artwork = new byte[]{1, 2, 3, 4, 5};
        Library library = new Library();
        library.addEntry(new Library.LibraryEntry(
                Paths.get("/music/song.mp3"),
                "Song",
                "Artist",
                "Album",
                Duration.ofMinutes(3),
                artwork,
                System.currentTimeMillis()
        ));

        repository.save(library);
        Library loaded = repository.load();

        assertThat(loaded.getEntries().get(0).hasArtwork()).isTrue();
        assertThat(loaded.getEntries().get(0).artwork()).isEqualTo(artwork);
    }

    @Test
    void shouldPreserveLastScanTimestamp() {
        Library library = new Library();
        library.addEntry(createEntry("song.mp3"));
        long timestamp = System.currentTimeMillis();
        library.setLastScanTimestamp(timestamp);

        repository.save(library);
        Library loaded = repository.load();

        assertThat(loaded.getLastScanTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void shouldLoadEmptyLibraryWhenNoFileExists() {
        Library loaded = repository.load();

        assertThat(loaded.getImportPaths()).isEmpty();
        assertThat(loaded.getEntries()).isEmpty();
    }

    @Test
    void shouldDeleteLibrary() {
        Library library = createLibraryWithOneEntry();
        repository.save(library);

        repository.delete();

        assertThat(repository.exists()).isFalse();
    }

    @Test
    void shouldNotThrowWhenDeletingNonExistentLibrary() {
        repository.delete();
        assertThat(repository.exists()).isFalse();
    }

    @Test
    void shouldHandleMultipleImportPaths() {
        Library library = new Library();
        library.addImportPath(Paths.get("/music1"));
        library.addImportPath(Paths.get("/music2"));
        library.addImportPath(Paths.get("/music3"));

        repository.save(library);
        Library loaded = repository.load();

        assertThat(loaded.getImportPaths())
                .containsExactlyInAnyOrder(
                        Paths.get("/music1"),
                        Paths.get("/music2"),
                        Paths.get("/music3")
                );
    }

    private Library createLibraryWithOneEntry() {
        Library library = new Library();
        library.addImportPath(Paths.get("/music"));
        library.addEntry(createEntry("song.mp3"));
        return library;
    }

    private Library.LibraryEntry createEntry(String filename) {
        return new Library.LibraryEntry(
                Paths.get("/music/" + filename),
                "Test Song",
                "Test Artist",
                "Test Album",
                Duration.ofMinutes(3),
                null,
                System.currentTimeMillis()
        );
    }
}
