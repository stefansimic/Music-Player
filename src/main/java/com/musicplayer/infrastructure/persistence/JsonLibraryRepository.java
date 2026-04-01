package com.musicplayer.infrastructure.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.musicplayer.domain.contract.LibraryRepository;
import com.musicplayer.domain.exception.LibraryStorageException;
import com.musicplayer.domain.model.Library;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * JSON-based implementation of LibraryRepository.
 * 
 * Stores the library in a JSON file in the user's home directory.
 */
public class JsonLibraryRepository implements LibraryRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonLibraryRepository.class);
    private static final String LIBRARY_DIR = ".musicplayer";
    private static final String LIBRARY_FILE = "library.json";
    
    private final ObjectMapper objectMapper;
    private final Path libraryPath;

    public JsonLibraryRepository() {
        this(createDefaultLibraryPath());
    }

    public JsonLibraryRepository(Path libraryPath) {
        this.libraryPath = libraryPath;
        this.objectMapper = createObjectMapper();
    }

    private static Path createDefaultLibraryPath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, LIBRARY_DIR, LIBRARY_FILE);
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    @Override
    public void save(Library library) throws LibraryStorageException {
        ensureDirectoryExists();
        
        Path tempPath = libraryPath.resolveSibling(libraryPath.getFileName() + ".tmp");
        
        try {
            LibraryDto dto = toDto(library);
            String json = objectMapper.writeValueAsString(dto);
            Files.writeString(tempPath, json);
            
            if (Files.exists(libraryPath)) {
                Path backupPath = libraryPath.resolveSibling(libraryPath.getFileName() + ".bak");
                Files.copy(libraryPath, backupPath);
            }
            
            Files.move(tempPath, libraryPath);
            logger.info("Library saved successfully to: {}", libraryPath);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize library to JSON", e);
            throw new LibraryStorageException("Failed to serialize library: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Failed to write library file", e);
            throw new LibraryStorageException("Failed to write library file: " + e.getMessage(), e);
        } finally {
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public Library load() throws LibraryStorageException {
        if (!exists()) {
            logger.info("No library file found, returning empty library");
            return new Library();
        }
        
        try {
            String json = Files.readString(libraryPath);
            LibraryDto dto = objectMapper.readValue(json, LibraryDto.class);
            logger.info("Library loaded successfully: {} entries", dto.entries.size());
            return fromDto(dto);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse library JSON", e);
            throw new LibraryStorageException("Failed to parse library: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Failed to read library file", e);
            throw new LibraryStorageException("Failed to read library file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists() {
        return Files.exists(libraryPath);
    }

    @Override
    public void delete() throws LibraryStorageException {
        try {
            Files.deleteIfExists(libraryPath);
            logger.info("Library file deleted");
            
            Path backupPath = libraryPath.resolveSibling(libraryPath.getFileName() + ".bak");
            Files.deleteIfExists(backupPath);
        } catch (IOException e) {
            logger.error("Failed to delete library file", e);
            throw new LibraryStorageException("Failed to delete library file: " + e.getMessage(), e);
        }
    }

    private void ensureDirectoryExists() throws LibraryStorageException {
        Path dir = libraryPath.getParent();
        if (dir != null && !Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new LibraryStorageException("Failed to create library directory: " + e.getMessage(), e);
            }
        }
    }

    private LibraryDto toDto(Library library) {
        LibraryDto dto = new LibraryDto();
        dto.importPaths = library.getImportPaths().stream()
                .map(Path::toString)
                .toList();
        dto.lastScanTimestamp = library.getLastScanTimestamp();
        dto.entries = library.getEntries().stream()
                .map(this::entryToDto)
                .toList();
        return dto;
    }

    private EntryDto entryToDto(Library.LibraryEntry entry) {
        EntryDto dto = new EntryDto();
        dto.path = entry.path().toString();
        dto.title = entry.title();
        dto.artist = entry.artist();
        dto.album = entry.album();
        dto.durationSeconds = entry.duration() != null ? entry.duration().getSeconds() : 0;
        dto.lastModified = entry.lastModified();
        if (entry.hasArtwork()) {
            dto.artworkBase64 = Base64.getEncoder().encodeToString(entry.artwork());
        }
        return dto;
    }

    private Library fromDto(LibraryDto dto) {
        List<Path> paths = dto.importPaths.stream()
                .map(Paths::get)
                .toList();
        
        List<Library.LibraryEntry> entries = dto.entries.stream()
                .map(this::dtoToEntry)
                .toList();
        
        return new Library(paths, entries, dto.lastScanTimestamp);
    }

    private Library.LibraryEntry dtoToEntry(EntryDto dto) {
        Path path = Paths.get(dto.path);
        String title = dto.title;
        String artist = dto.artist;
        String album = dto.album;
        java.time.Duration duration = dto.durationSeconds > 0 
                ? java.time.Duration.ofSeconds(dto.durationSeconds) 
                : null;
        byte[] artwork = dto.artworkBase64 != null 
                ? Base64.getDecoder().decode(dto.artworkBase64) 
                : null;
        
        return new Library.LibraryEntry(path, title, artist, album, duration, artwork, dto.lastModified);
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LibraryDto {
        public List<String> importPaths = new ArrayList<>();
        public long lastScanTimestamp;
        public List<EntryDto> entries = new ArrayList<>();
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EntryDto {
        public String path;
        public String title;
        public String artist;
        public String album;
        public long durationSeconds;
        public long lastModified;
        public String artworkBase64;
    }
}
