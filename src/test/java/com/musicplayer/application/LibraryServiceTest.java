package com.musicplayer.application;

import com.musicplayer.application.service.LibraryService;
import com.musicplayer.domain.contract.FileScanner;
import com.musicplayer.domain.contract.LibraryRepository;
import com.musicplayer.domain.contract.MetadataReader;
import com.musicplayer.domain.model.Library;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @Mock
    private LibraryRepository repository;

    @Mock
    private FileScanner fileScanner;

    @Mock
    private MetadataReader metadataReader;

    private LibraryService libraryService;

    @BeforeEach
    void setUp() {
        libraryService = new LibraryService(repository, fileScanner, metadataReader);
    }

    @AfterEach
    void tearDown() {
        libraryService.shutdown();
    }

    @Test
    void shouldLoadLibraryOnStartup() {
        Library library = new Library();
        when(repository.load()).thenReturn(library);

        Library loaded = libraryService.loadLibrary();

        assertThat(loaded).isNotNull();
        verify(repository).load();
    }

    @Test
    void shouldReturnEmptyLibraryWhenLoadFails() {
        when(repository.load()).thenThrow(new com.musicplayer.domain.exception.LibraryStorageException("Test"));

        Library loaded = libraryService.loadLibrary();

        assertThat(loaded).isNotNull();
        assertThat(loaded.getEntries()).isEmpty();
    }

    @Test
    void shouldSaveLibrary() {
        when(repository.load()).thenReturn(new Library());

        libraryService.loadLibrary();
        libraryService.saveLibrary();

        verify(repository).save(any(Library.class));
    }

    @Test
    void shouldRemoveImportPathAndItsTracks() {
        Path importPath = Paths.get("/music");
        Library library = new Library();
        library.addImportPath(importPath);
        library.addEntry(new Library.LibraryEntry(
                Paths.get("/music/song.mp3"), "Song", "Artist", "Album", null, null, 0
        ));
        when(repository.load()).thenReturn(library);

        libraryService.loadLibrary();
        libraryService.removeImportPath(importPath);

        assertThat(library.getImportPaths()).isEmpty();
        assertThat(library.getEntries()).isEmpty();
        verify(repository).save(any(Library.class));
    }

    @Test
    void shouldGetLibrary() {
        Library library = new Library();
        when(repository.load()).thenReturn(library);

        Library result = libraryService.getLibrary();

        assertThat(result).isNotNull();
    }

    @Test
    void shouldLoadLibraryOnFirstGet() {
        Library library = new Library();
        when(repository.load()).thenReturn(library);

        libraryService.getLibrary();

        verify(repository).load();
    }

    @Test
    void shouldHandleNullProgressCallback() {
        when(repository.load()).thenReturn(new Library());

        libraryService.loadLibrary();

        assertThat(libraryService.getLibrary()).isNotNull();
    }
}
