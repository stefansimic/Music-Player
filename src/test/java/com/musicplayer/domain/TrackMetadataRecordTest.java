package com.musicplayer.domain;

import com.musicplayer.domain.model.Track;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrackMetadataRecordTest {

    @Test
    void shouldDefaultNullTitleToUnknown() {
        Track track = new Track("/music/test.mp3", null, "Artist", "Album", null);

        assertThat(track.getTitle()).isEqualTo("test");
    }

    @Test
    void shouldDefaultNullArtistToUnknown() {
        Track track = new Track("/music/test.mp3", "Title", null, "Album", null);

        assertThat(track.getArtist()).isEqualTo("Unknown");
    }

    @Test
    void shouldDefaultNullAlbumToUnknown() {
        Track track = new Track("/music/test.mp3", "Title", "Artist", null, null);

        assertThat(track.getAlbum()).isEqualTo("Unknown");
    }

    @Test
    void shouldPreserveValidMetadata() {
        Track track = new Track("/music/test.mp3", "My Song", "My Artist", "My Album", null);

        assertThat(track.getTitle()).isEqualTo("My Song");
        assertThat(track.getArtist()).isEqualTo("My Artist");
        assertThat(track.getAlbum()).isEqualTo("My Album");
    }

    @Test
    void shouldHandleBlankStringsAsUnknown() {
        Track track = new Track("/music/test.mp3", "  ", "\t", "", null);

        assertThat(track.getTitle()).isEqualTo("test");
        assertThat(track.getArtist()).isEqualTo("Unknown");
        assertThat(track.getAlbum()).isEqualTo("Unknown");
    }

    @Test
    void shouldExtractTitleFromPathWithBackslash() {
        Track track = new Track("C:\\Music\\MySong.mp3");

        assertThat(track.getTitle()).isEqualTo("MySong");
    }

    @Test
    void shouldHandlePathWithMultipleDots() {
        Track track = new Track("/music/song.v2.final.mp3");

        assertThat(track.getTitle()).isEqualTo("song.v2.final");
    }

    @Test
    void shouldHandlePathWithoutExtension() {
        Track track = new Track("/music/MySong");

        assertThat(track.getTitle()).isEqualTo("MySong");
    }

    @Test
    void shouldUpdateMetadataViaSetters() {
        Track track = new Track("/music/test.mp3");

        track.setTitle("New Title");
        track.setArtist("New Artist");
        track.setAlbum("New Album");

        assertThat(track.getTitle()).isEqualTo("New Title");
        assertThat(track.getArtist()).isEqualTo("New Artist");
        assertThat(track.getAlbum()).isEqualTo("New Album");
    }

    @Test
    void shouldSetSettersDefaultNullToUnknown() {
        Track track = new Track("/music/test.mp3");

        track.setTitle(null);
        track.setArtist(null);
        track.setAlbum(null);

        assertThat(track.getTitle()).isEqualTo("Unknown");
        assertThat(track.getArtist()).isEqualTo("Unknown");
        assertThat(track.getAlbum()).isEqualTo("Unknown");
    }
}
