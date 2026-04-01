package com.musicplayer.infrastructure;

import com.musicplayer.infrastructure.metadata.JAudioTaggerMetadataReader;
import com.musicplayer.domain.contract.MetadataReader;
import com.musicplayer.domain.exception.MetadataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JAudioTaggerMetadataReaderTest {

    private JAudioTaggerMetadataReader reader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        reader = new JAudioTaggerMetadataReader();
    }

    @Test
    void shouldReadMetadataFromTestFiles() throws IOException {
        Path testFile = findTestMp3File();
        if (testFile == null) {
            return;
        }

        MetadataReader.TrackMetadata metadata = reader.read(testFile);

        assertThat(metadata).isNotNull();
    }

    @Test
    void shouldRejectNullPath() {
        assertThrows(MetadataException.class, () -> reader.read(null));
    }

    @Test
    void shouldRejectNonExistentFile() {
        Path nonExistent = tempDir.resolve("non-existent.mp3");

        assertThrows(MetadataException.class, () -> reader.read(nonExistent));
    }

    @Test
    void shouldRejectDirectory() throws IOException {
        Path dir = Files.createDirectory(tempDir.resolve("folder"));

        assertThrows(MetadataException.class, () -> reader.read(dir));
    }

    @Test
    void shouldReturnDurationFromValidFile() throws IOException {
        Path testFile = findTestMp3File();
        if (testFile == null) {
            return;
        }

        MetadataReader.TrackMetadata metadata = reader.read(testFile);

        if (metadata.duration() != null) {
            assertThat(metadata.duration()).isGreaterThan(Duration.ZERO);
        }
    }

    @Test
    void shouldHandleUnicodeFilenames() throws IOException {
        Path testFile = findTestMp3File();
        if (testFile == null) {
            return;
        }

        Path unicodePath = tempDir.resolve("Тест.mp3");
        Files.copy(testFile, unicodePath);

        MetadataReader.TrackMetadata metadata = reader.read(unicodePath);

        assertThat(metadata).isNotNull();
    }

    @Test
    void shouldHandleSerbianCharactersInFilepath() throws IOException {
        Path testFile = findTestMp3File();
        if (testFile == null) {
            return;
        }

        Path serbianPath = tempDir.resolve("Ti Odlaziš Ja Ostajem.mp3");
        Files.copy(testFile, serbianPath);

        MetadataReader.TrackMetadata metadata = reader.read(serbianPath);

        assertThat(metadata).isNotNull();
    }

    private Path findTestMp3File() {
        try {
            Path testDataDir = Path.of("src/test/resources/test-data");
            if (Files.exists(testDataDir)) {
                return Files.list(testDataDir)
                    .filter(p -> p.toString().endsWith(".mp3"))
                    .findFirst()
                    .orElse(null);
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
}
