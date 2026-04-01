package com.musicplayer.domain;

import com.musicplayer.domain.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrackTest {

    @Test
    void shouldCreateTrackFromPathOnly() {
        Track track = new Track("/music/song.mp3");

        assertThat(track.getPath()).isEqualTo("/music/song.mp3");
        assertThat(track.getTitle()).isEqualTo("song");
        assertThat(track.getArtist()).isEqualTo("Unknown");
        assertThat(track.getAlbum()).isEqualTo("Unknown");
        assertThat(track.getDuration()).isNull();
    }

    @Test
    void shouldExtractTitleFromPath() {
        Track track = new Track("/music/My Favorite Song.mp3");

        assertThat(track.getTitle()).isEqualTo("My Favorite Song");
    }

    @Test
    void shouldHandlePathWithoutDirectory() {
        Track track = new Track("song.mp3");

        assertThat(track.getTitle()).isEqualTo("song");
    }

    @Test
    void shouldCreateTrackWithFullMetadata() {
        Duration duration = Duration.ofMinutes(3).plusSeconds(45);
        Track track = new Track("/music/song.mp3", "My Song", "My Artist", "My Album", duration);

        assertThat(track.getTitle()).isEqualTo("My Song");
        assertThat(track.getArtist()).isEqualTo("My Artist");
        assertThat(track.getAlbum()).isEqualTo("My Album");
        assertThat(track.getDuration()).isEqualTo(duration);
    }

    @Test
    void shouldHandleNullMetadataAsUnknown() {
        Track track = new Track("/music/song.mp3", null, null, null, null);

        assertThat(track.getTitle()).isEqualTo("song");
        assertThat(track.getArtist()).isEqualTo("Unknown");
        assertThat(track.getAlbum()).isEqualTo("Unknown");
    }

    @Test
    void shouldHandleBlankMetadataAsUnknown() {
        Track track = new Track("/music/song.mp3", "  ", "\t", "", null);

        assertThat(track.getTitle()).isEqualTo("song");
        assertThat(track.getArtist()).isEqualTo("Unknown");
        assertThat(track.getAlbum()).isEqualTo("Unknown");
    }

    @Test
    void shouldRejectNullPath() {
        assertThrows(IllegalArgumentException.class, () -> new Track(null));
    }

    @Test
    void shouldRejectEmptyPath() {
        assertThrows(IllegalArgumentException.class, () -> new Track(""));
    }

    @Test
    void shouldUpdateMetadata() {
        Track track = new Track("/music/song.mp3");

        track.setTitle("New Title");
        track.setArtist("New Artist");
        track.setAlbum("New Album");

        assertThat(track.getTitle()).isEqualTo("New Title");
        assertThat(track.getArtist()).isEqualTo("New Artist");
        assertThat(track.getAlbum()).isEqualTo("New Album");
    }

    @Test
    void shouldBeEqualByPath() {
        Track track1 = new Track("/music/song.mp3");
        Track track2 = new Track("/music/song.mp3");
        Track track3 = new Track("/music/other.mp3");

        assertThat(track1).isEqualTo(track2);
        assertThat(track1).isNotEqualTo(track3);
    }

    @Test
    void shouldHaveSameHashCodeForEqualPaths() {
        Track track1 = new Track("/music/song.mp3");
        Track track2 = new Track("/music/song.mp3");

        assertThat(track1.hashCode()).isEqualTo(track2.hashCode());
    }

    @Test
    void shouldReturnCorrectToString() {
        Track track = new Track("/music/song.mp3", "Song", "Artist", "Album", Duration.ofMinutes(3));

        String toString = track.toString();

        assertThat(toString).contains("Song");
        assertThat(toString).contains("Artist");
        assertThat(toString).contains("Album");
    }
}
