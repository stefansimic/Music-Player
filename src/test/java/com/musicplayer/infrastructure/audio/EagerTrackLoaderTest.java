package com.musicplayer.infrastructure.audio;

import com.musicplayer.domain.contract.AudioPlayer;
import com.musicplayer.domain.contract.TrackLoader;
import com.musicplayer.domain.exception.PlaybackException;
import com.musicplayer.domain.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EagerTrackLoaderTest {

    private AudioPlayer mockAudioPlayer;
    private EagerTrackLoader trackLoader;
    private List<Track> testTracks;

    @BeforeEach
    void setUp() {
        mockAudioPlayer = mock(AudioPlayer.class);
        trackLoader = new EagerTrackLoader(mockAudioPlayer);
        
        testTracks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            testTracks.add(new Track("/music/track" + i + ".mp3", "Track " + i, "Artist", "Album", null));
        }
    }

    @Test
    void shouldDelegatePlayToAudioPlayer() throws PlaybackException {
        trackLoader.initialize(testTracks);
        trackLoader.loadTrack(testTracks.get(0));
        
        verify(mockAudioPlayer).play(testTracks.get(0));
    }

    @Test
    void shouldReturnAllTracksAsPreloaded() {
        trackLoader.initialize(testTracks);
        
        List<Track> preloaded = trackLoader.getPreloadedTracks();
        assertThat(preloaded).hasSize(5);
        assertThat(preloaded).containsAll(testTracks);
    }

    @Test
    void shouldAlwaysReportTrackAsLoaded() {
        trackLoader.initialize(testTracks);
        
        assertThat(trackLoader.isTrackLoaded(testTracks.get(0))).isTrue();
        assertThat(trackLoader.isTrackLoaded(testTracks.get(2))).isTrue();
    }

    @Test
    void shouldReportResourceSavingModeDisabled() {
        assertThat(trackLoader.isResourceSavingMode()).isFalse();
    }

    @Test
    void shouldIgnoreResourceSavingToggle() {
        trackLoader.setResourceSavingMode(true);
        assertThat(trackLoader.isResourceSavingMode()).isFalse();
    }

    @Test
    void shouldDoNothingOnAdvancePreload() {
        trackLoader.initialize(testTracks);
        Track result = trackLoader.advancePreload(2, 5);
        
        assertThat(result).isNull();
    }

    @Test
    void shouldDoNothingOnRewindPreload() {
        trackLoader.initialize(testTracks);
        Track result = trackLoader.rewindPreload(1);
        
        assertThat(result).isNull();
    }

    @Test
    void shouldDoNothingOnUnloadAll() {
        trackLoader.initialize(testTracks);
        trackLoader.unloadAll();
        
        assertThat(trackLoader.getPreloadedTracks()).hasSize(5);
    }

    @Test
    void shouldHandleNullTrack() throws PlaybackException {
        trackLoader.initialize(testTracks);
        trackLoader.loadTrack(null);
        
        verify(mockAudioPlayer, never()).play(any());
    }
}
