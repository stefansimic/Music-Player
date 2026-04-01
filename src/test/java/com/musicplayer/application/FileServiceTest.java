package com.musicplayer.application;

import com.musicplayer.application.service.PlaylistService;
import com.musicplayer.application.service.FileService;
import com.musicplayer.domain.contract.FileScanner;
import com.musicplayer.domain.contract.MetadataReader;
import com.musicplayer.domain.exception.FileAccessException;
import com.musicplayer.domain.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileServiceTest {

    private FileService fileService;
    private FileScanner mockScanner;
    private MetadataReader mockReader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mockScanner = mock(FileScanner.class);
        mockReader = mock(MetadataReader.class);
        fileService = new FileService(mockScanner, mockReader, true);
    }

    @Test
    void shouldLoadTracksFromDirectory() throws FileAccessException {
        when(mockScanner.scanAndSort(tempDir, true))
            .thenReturn(List.of(Path.of("/music/song.mp3")));
        
        when(mockReader.read(Path.of("/music/song.mp3")))
            .thenReturn(new MetadataReader.TrackMetadata("Test Song", "Test Artist", "Test Album", null));

        List<Track> tracks = fileService.loadDirectory(tempDir, null);

        assertThat(tracks).hasSize(1);
        assertThat(tracks.get(0).getTitle()).isEqualTo("Test Song");
        assertThat(tracks.get(0).getArtist()).isEqualTo("Test Artist");
    }

    @Test
    void shouldReturnEmptyListForEmptyDirectory() throws FileAccessException {
        when(mockScanner.scanAndSort(tempDir, true))
            .thenReturn(List.of());

        List<Track> tracks = fileService.loadDirectory(tempDir, null);

        assertThat(tracks).isEmpty();
    }

    @Test
    void shouldHandleMetadataReadFailure() throws FileAccessException {
        when(mockScanner.scanAndSort(tempDir, true))
            .thenReturn(List.of(Path.of("/music/bad-file.mp3")));
        
        when(mockReader.read(Path.of("/music/bad-file.mp3")))
            .thenThrow(new com.musicplayer.domain.exception.MetadataException("Cannot read metadata"));

        List<Track> tracks = fileService.loadDirectory(tempDir, null);

        assertThat(tracks).hasSize(1);
        assertThat(tracks.get(0).getTitle()).isEqualTo("bad-file");
    }

    @Test
    void shouldUseFilenameAsTitleWhenMetadataMissing() throws FileAccessException {
        when(mockScanner.scanAndSort(tempDir, true))
            .thenReturn(List.of(Path.of("/music/untitled.mp3")));
        
        when(mockReader.read(Path.of("/music/untitled.mp3")))
            .thenReturn(new MetadataReader.TrackMetadata("", "", "", null));

        List<Track> tracks = fileService.loadDirectory(tempDir, null);

        assertThat(tracks).hasSize(1);
        assertThat(tracks.get(0).getTitle()).isEqualTo("Unknown");
        assertThat(tracks.get(0).getArtist()).isEqualTo("Unknown");
    }

    @Test
    void shouldReportProgress() throws FileAccessException {
        when(mockScanner.scanAndSort(tempDir, true))
            .thenReturn(List.of(
                Path.of("/music/song1.mp3"),
                Path.of("/music/song2.mp3")
            ));
        
        when(mockReader.read(org.mockito.ArgumentMatchers.any()))
            .thenReturn(new MetadataReader.TrackMetadata("Song", "Artist", null, null));

        int[] progress = {0, 0};
        
        fileService.loadDirectory(tempDir, (current, total, file) -> {
            progress[0] = current;
            progress[1] = total;
        });

        assertThat(progress[0]).isEqualTo(2);
        assertThat(progress[1]).isEqualTo(2);
    }
}
