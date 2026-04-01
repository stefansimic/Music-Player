package com.musicplayer.domain;

import com.musicplayer.domain.model.Playlist;
import com.musicplayer.domain.model.RepeatMode;
import com.musicplayer.domain.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlaylistTest {

    private Playlist playlist;
    private Track track1;
    private Track track2;
    private Track track3;

    @BeforeEach
    void setUp() {
        playlist = new Playlist("Test Playlist");
        track1 = new Track("/music/song1.mp3", "Song One", "Artist A", "Album A", Duration.ofMinutes(3));
        track2 = new Track("/music/song2.mp3", "Song Two", "Artist B", "Album B", Duration.ofMinutes(4));
        track3 = new Track("/music/song3.mp3", "Song Three", "Artist C", "Album C", Duration.ofMinutes(5));
    }

    @Test
    void shouldCreateEmptyPlaylist() {
        assertThat(playlist.isEmpty()).isTrue();
        assertThat(playlist.size()).isZero();
        assertThat(playlist.getCurrentTrack()).isNull();
        assertThat(playlist.getCurrentIndex()).isEqualTo(-1);
    }

    @Test
    void shouldAddTrack() {
        playlist.addTrack(track1);

        assertThat(playlist.size()).isEqualTo(1);
        assertThat(playlist.getCurrentTrack()).isEqualTo(track1);
        assertThat(playlist.getCurrentIndex()).isZero();
    }

    @Test
    void shouldRejectNullTrack() {
        assertThrows(IllegalArgumentException.class, () -> playlist.addTrack(null));
    }

    @Test
    void shouldRemoveTrack() {
        playlist.addTrack(track1);
        playlist.addTrack(track2);

        playlist.removeTrack(0);

        assertThat(playlist.size()).isEqualTo(1);
        assertThat(playlist.getTrack(0)).isEqualTo(track2);
    }

    @Test
    void shouldRejectInvalidIndexForGetTrack() {
        assertThrows(IndexOutOfBoundsException.class, () -> playlist.getTrack(0));
    }

    @Test
    void shouldRejectInvalidIndexForRemoveTrack() {
        playlist.addTrack(track1);
        assertThrows(IndexOutOfBoundsException.class, () -> playlist.removeTrack(5));
    }

    @Test
    void shouldNavigateToNextTrack() {
        playlist.addTrack(track1);
        playlist.addTrack(track2);
        playlist.addTrack(track3);

        Track next1 = playlist.getNextTrack();
        Track next2 = playlist.getNextTrack();

        assertThat(next1).isEqualTo(track2);
        assertThat(next2).isEqualTo(track3);
        assertThat(playlist.getCurrentIndex()).isEqualTo(2);
    }

    @Test
    void shouldLoopToFirstTrack() {
        playlist.addTrack(track1);
        playlist.addTrack(track2);

        playlist.getNextTrack();
        Track looped = playlist.getNextTrack();

        assertThat(looped).isEqualTo(track1);
    }

    @Test
    void shouldNavigateToPreviousTrack() {
        playlist.addTrack(track1);
        playlist.addTrack(track2);
        playlist.setCurrentIndex(1);

        Track previous = playlist.getPreviousTrack();

        assertThat(previous).isEqualTo(track1);
    }

    @Test
    void shouldLoopFromFirstToLast() {
        playlist.addTrack(track1);
        playlist.addTrack(track2);

        playlist.getPreviousTrack();

        assertThat(playlist.getCurrentTrack()).isEqualTo(track2);
    }

    @Test
    void shouldClearPlaylist() {
        playlist.addTrack(track1);
        playlist.addTrack(track2);

        playlist.clear();

        assertThat(playlist.isEmpty()).isTrue();
        assertThat(playlist.getCurrentTrack()).isNull();
    }

    @Test
    void shouldShufflePlaylist() {
        playlist.addTrack(track1);
        playlist.addTrack(track2);
        playlist.addTrack(track3);

        playlist.shuffle();

        assertThat(playlist.isShuffled()).isTrue();
        assertThat(playlist.size()).isEqualTo(3);
        assertThat(playlist.getCurrentIndex()).isZero();
    }

    @Test
    void shouldUnshufflePlaylist() {
        playlist.addTrack(track1);
        playlist.addTrack(track2);

        playlist.shuffle();
        playlist.unshuffle();

        assertThat(playlist.isShuffled()).isFalse();
        assertThat(playlist.getTrack(0)).isEqualTo(track1);
        assertThat(playlist.getTrack(1)).isEqualTo(track2);
    }

    @Test
    void shouldIterateOverPlaylist() {
        playlist.addTrack(track1);
        playlist.addTrack(track2);

        Iterator<Track> iterator = playlist.iterator();

        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).isEqualTo(track1);
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).isEqualTo(track2);
        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    void shouldReturnUnmodifiableTrackList() {
        playlist.addTrack(track1);

        assertThrows(UnsupportedOperationException.class, 
            () -> playlist.getTracks().add(track2));
    }

    @Test
    void shouldSetRepeatMode() {
        assertThat(playlist.getRepeatMode()).isEqualTo(RepeatMode.OFF);

        playlist.setRepeatMode(RepeatMode.ALL);

        assertThat(playlist.getRepeatMode()).isEqualTo(RepeatMode.ALL);
    }

    @Test
    void shouldCycleRepeatMode() {
        assertThat(playlist.nextRepeatMode()).isEqualTo(RepeatMode.ALL);
        assertThat(playlist.nextRepeatMode()).isEqualTo(RepeatMode.ONE);
        assertThat(playlist.nextRepeatMode()).isEqualTo(RepeatMode.OFF);
    }

    @Test
    void shouldMoveTrack() {
        playlist.addTrack(track1);
        playlist.addTrack(track2);
        playlist.addTrack(track3);

        playlist.moveTrack(0, 2);

        assertThat(playlist.getTrack(0)).isEqualTo(track2);
        assertThat(playlist.getTrack(2)).isEqualTo(track1);
    }

    @Test
    void shouldHandleNextWithRepeatOne() {
        playlist.addTrack(track1);
        playlist.addTrack(track2);
        playlist.setRepeatMode(RepeatMode.ONE);

        Track repeated = playlist.getNextTrack();

        assertThat(repeated).isEqualTo(track1);
    }

    @Test
    void shouldReturnUnmodifiableTracks() {
        playlist.addTrack(track1);

        var tracks = playlist.getTracks();

        assertThat(tracks).containsExactly(track1);
    }
}
