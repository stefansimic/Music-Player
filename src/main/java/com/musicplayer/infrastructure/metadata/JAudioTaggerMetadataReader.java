package com.musicplayer.infrastructure.metadata;

import com.musicplayer.domain.contract.MetadataReader;
import com.musicplayer.domain.exception.MetadataException;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;

/**
 * JAudioTagger-based implementation of the MetadataReader contract.
 * 
 * Reads ID3 tags and audio information from MP3 files.
 * Handles various character encodings including Unicode, Latin, and Cyrillic.
 */
public class JAudioTaggerMetadataReader implements MetadataReader {
    
    private static final Logger logger = LoggerFactory.getLogger(JAudioTaggerMetadataReader.class);

    @Override
    public TrackMetadata read(Path filePath) throws MetadataException {
        validateFilePath(filePath);
        
        try {
            AudioFile audioFile = AudioFileIO.read(filePath.toFile());
            Tag tag = audioFile.getTag();
            AudioHeader header = audioFile.getAudioHeader();
            
            String title = extractTagValue(tag, FieldKey.TITLE);
            String artist = extractTagValue(tag, FieldKey.ARTIST);
            String album = extractTagValue(tag, FieldKey.ALBUM);
            Duration duration = extractDuration(header);
            byte[] artwork = extractArtwork(tag);
            
            return new TrackMetadata(title, artist, album, duration, artwork);
        } catch (Exception e) {
            logger.warn("Failed to read metadata from: {}", filePath, e);
            throw new MetadataException("Failed to read metadata from: " + filePath, e);
        }
    }

    @Override
    public byte[] readArtwork(Path filePath) throws MetadataException {
        validateFilePath(filePath);
        
        try {
            AudioFile audioFile = AudioFileIO.read(filePath.toFile());
            Tag tag = audioFile.getTag();
            return extractArtwork(tag);
        } catch (Exception e) {
            logger.warn("Failed to read artwork from: {}", filePath, e);
            throw new MetadataException("Failed to read artwork from: " + filePath, e);
        }
    }

    private void validateFilePath(Path filePath) throws MetadataException {
        if (filePath == null) {
            throw new MetadataException("File path cannot be null");
        }
        if (!filePath.toFile().exists()) {
            throw new MetadataException("File does not exist: " + filePath);
        }
        if (!filePath.toFile().canRead()) {
            throw new MetadataException("File cannot be read: " + filePath);
        }
    }

    private String extractTagValue(Tag tag, FieldKey key) {
        if (tag == null) {
            return null;
        }
        try {
            String value = tag.getFirst(key);
            if (value == null || value.isBlank()) {
                return null;
            }
            return fixEncoding(value);
        } catch (Exception e) {
            logger.debug("Failed to extract tag {}: {}", key, e.getMessage());
            return null;
        }
    }

    private byte[] extractArtwork(Tag tag) {
        if (tag == null) {
            return null;
        }
        try {
            Artwork artwork = tag.getFirstArtwork();
            if (artwork != null && artwork.getBinaryData() != null) {
                return artwork.getBinaryData();
            }
        } catch (Exception e) {
            logger.debug("Failed to extract artwork: {}", e.getMessage());
        }
        return null;
    }

    private String fixEncoding(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (isValidUtf8(value)) {
            return value;
        }
        
        Charset[] charsetsToTry = {
            StandardCharsets.ISO_8859_1,
            Charset.forName("ISO-8859-2"),
            Charset.forName("ISO-8859-7"),
            Charset.forName("windows-1251"),
            Charset.forName("windows-1252"),
            Charset.forName("ISO-8859-5")
        };
        
        for (Charset srcCharset : charsetsToTry) {
            String converted = convertCharset(value, srcCharset, StandardCharsets.UTF_8);
            if (converted != null && isValidUtf8(converted)) {
                return converted;
            }
        }
        
        return value;
    }

    private String convertCharset(String value, Charset srcCharset, Charset dstCharset) {
        try {
            CharsetDecoder decoder = srcCharset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
            
            ByteBuffer inputBytes = ByteBuffer.wrap(value.getBytes(srcCharset));
            CharBuffer result = decoder.decode(inputBytes);
            return result.toString();
        } catch (CharacterCodingException e) {
            return null;
        }
    }

    private boolean isValidUtf8(String value) {
        try {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            String reconstructed = new String(bytes, StandardCharsets.UTF_8);
            return reconstructed.equals(value);
        } catch (Exception e) {
            return false;
        }
    }

    private Duration extractDuration(AudioHeader header) {
        if (header == null) {
            return null;
        }
        try {
            int seconds = header.getTrackLength();
            return Duration.ofSeconds(seconds);
        } catch (Exception e) {
            logger.debug("Failed to extract duration: {}", e.getMessage());
            return null;
        }
    }
}
