package com.musicplayer.domain;

import com.musicplayer.domain.model.Library;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class LibraryTest {

    @Test
    void shouldCreateEmptyLibrary() {
        Library library = new Library();

        assertThat(library.getImportPaths()).isEmpty();
        assertThat(library.getEntries()).isEmpty();
        assertThat(library.getEntryCount()).isZero();
        assertThat(library.getLastScanTimestamp()).isZero();
    }

    @Test
    void shouldAddImportPath() {
        Library library = new Library();
        Path path = Paths.get("/music");

        library.addImportPath(path);

        assertThat(library.getImportPaths()).containsExactly(path);
    }

    @Test
    void shouldNotAddDuplicateImportPath() {
        Library library = new Library();
        Path path = Paths.get("/music");

        library.addImportPath(path);
        library.addImportPath(path);

        assertThat(library.getImportPaths()).hasSize(1);
    }

    @Test
    void shouldNotAddNullImportPath() {
        Library library = new Library();

        library.addImportPath(null);

        assertThat(library.getImportPaths()).isEmpty();
    }

    @Test
    void shouldRemoveImportPath() {
        Library library = new Library();
        Path path = Paths.get("/music");
        library.addImportPath(path);

        boolean removed = library.removeImportPath(path);

        assertThat(removed).isTrue();
        assertThat(library.getImportPaths()).isEmpty();
    }

    @Test
    void shouldAddEntry() {
        Library library = new Library();
        Library.LibraryEntry entry = createEntry(Paths.get("/music/song.mp3"), "Song", "Artist", "Album");

        library.addEntry(entry);

        assertThat(library.getEntries()).hasSize(1);
        assertThat(library.getEntryCount()).isEqualTo(1);
    }

    @Test
    void shouldNotAddNullEntry() {
        Library library = new Library();

        library.addEntry(null);

        assertThat(library.getEntries()).isEmpty();
    }

    @Test
    void shouldAddMultipleEntries() {
        Library library = new Library();
        library.addEntry(createEntry(Paths.get("/music/song1.mp3"), "Song1", "Artist1", "Album1"));
        library.addEntry(createEntry(Paths.get("/music/song2.mp3"), "Song2", "Artist2", "Album2"));

        assertThat(library.getEntries()).hasSize(2);
    }

    @Test
    void shouldRemoveEntry() {
        Library library = new Library();
        Path path = Paths.get("/music/song.mp3");
        library.addEntry(createEntry(path, "Song", "Artist", "Album"));

        boolean removed = library.removeEntry(path);

        assertThat(removed).isTrue();
        assertThat(library.getEntries()).isEmpty();
    }

    @Test
    void shouldClearEntries() {
        Library library = new Library();
        library.addEntry(createEntry(Paths.get("/music/song1.mp3"), "Song1", "Artist1", "Album1"));
        library.addEntry(createEntry(Paths.get("/music/song2.mp3"), "Song2", "Artist2", "Album2"));

        library.clearEntries();

        assertThat(library.getEntries()).isEmpty();
        assertThat(library.getEntryCount()).isZero();
    }

    @Test
    void shouldSetLastScanTimestamp() {
        Library library = new Library();
        long timestamp = System.currentTimeMillis();

        library.setLastScanTimestamp(timestamp);

        assertThat(library.getLastScanTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void shouldReturnUnmodifiableImportPaths() {
        Library library = new Library();
        library.addImportPath(Paths.get("/music"));

        assertThat(library.getImportPaths()).isUnmodifiable();
    }

    @Test
    void shouldReturnUnmodifiableEntries() {
        Library library = new Library();
        library.addEntry(createEntry(Paths.get("/music/song.mp3"), "Song", "Artist", "Album"));

        assertThat(library.getEntries()).isUnmodifiable();
    }

    @Test
    void libraryEntryShouldReturnDefaults() {
        Library.LibraryEntry entry = new Library.LibraryEntry(
                Paths.get("/music/song.mp3"),
                null,
                null,
                null,
                null,
                null,
                0
        );

        assertThat(entry.title()).isEqualTo("Unknown");
        assertThat(entry.artist()).isEqualTo("Unknown");
        assertThat(entry.album()).isEqualTo("Unknown");
        assertThat(entry.hasArtwork()).isFalse();
    }

    @Test
    void libraryEntryShouldDetectArtwork() {
        byte[] artwork = new byte[]{1, 2, 3};
        Library.LibraryEntry entry = new Library.LibraryEntry(
                Paths.get("/music/song.mp3"),
                "Song",
                "Artist",
                "Album",
                Duration.ofMinutes(3),
                artwork,
                System.currentTimeMillis()
        );

        assertThat(entry.hasArtwork()).isTrue();
    }

    private Library.LibraryEntry createEntry(Path path, String title, String artist, String album) {
        return new Library.LibraryEntry(
                path,
                title,
                artist,
                album,
                Duration.ofMinutes(3),
                null,
                System.currentTimeMillis()
        );
    }
}
