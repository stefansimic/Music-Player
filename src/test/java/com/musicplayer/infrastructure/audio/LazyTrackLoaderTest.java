package com.musicplayer.infrastructure.audio;

import com.musicplayer.domain.contract.AudioPlayer;
import com.musicplayer.domain.contract.TrackLoader;
import com.musicplayer.domain.exception.PlaybackException;
import com.musicplayer.domain.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

class LazyTrackLoaderTest {

    private AudioPlayer mockAudioPlayer;
    private LazyTrackLoader trackLoader;
    private List<Track> testTracks;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mockAudioPlayer = mock(AudioPlayer.class);
        trackLoader = new LazyTrackLoader(mockAudioPlayer, 3);
        
        testTracks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            testTracks.add(new Track("/music/track" + i + ".mp3", "Track " + i, "Artist", "Album", null));
        }
    }

    @Test
    void shouldPreloadTracksForCurrentPosition() {
        trackLoader.initialize(testTracks);
        trackLoader.loadTrack(testTracks.get(0));
        
        await().atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> assertThat(trackLoader.getPreloadedTracks()).hasSize(3));
        
        List<Track> preloaded = trackLoader.getPreloadedTracks();
        assertThat(preloaded).contains(testTracks.get(0), testTracks.get(1), testTracks.get(2));
    }

    @Test
    void shouldEvictOldestTrackOnAdvance() {
        trackLoader.initialize(testTracks);
        trackLoader.loadTrack(testTracks.get(0));
        
        await().atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> assertThat(trackLoader.getPreloadedTracks()).hasSize(3));
        
        assertThat(trackLoader.getPreloadedTracks()).contains(testTracks.get(0), testTracks.get(1), testTracks.get(2));
        
        trackLoader.advancePreload(0, 10);
        
        assertThat(trackLoader.getPreloadedTracks()).hasSize(3);
        assertThat(trackLoader.isTrackLoaded(testTracks.get(0))).isFalse();
        assertThat(trackLoader.isTrackLoaded(testTracks.get(3))).isTrue();
    }

    @Test
    void shouldReloadQueueOnRewind() {
        trackLoader.initialize(testTracks);
        trackLoader.loadTrack(testTracks.get(5));
        
        await().atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> assertThat(trackLoader.getPreloadedTracks()).hasSize(3));
        
        trackLoader.rewindPreload(2);
        
        await().atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> assertThat(trackLoader.getPreloadedTracks()).hasSize(3));
        
        List<Track> preloaded = trackLoader.getPreloadedTracks();
        assertThat(preloaded).contains(testTracks.get(2), testTracks.get(3), testTracks.get(4));
    }

    @Test
    void shouldHandleEndOfPlaylist() {
        trackLoader.initialize(testTracks);
        trackLoader.loadTrack(testTracks.get(8));
        
        await().atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> assertThat(trackLoader.getPreloadedTracks()).hasSize(2));
        
        List<Track> preloaded = trackLoader.getPreloadedTracks();
        assertThat(preloaded).contains(testTracks.get(8), testTracks.get(9));
    }

    @Test
    void shouldUnloadAllTracks() {
        trackLoader.initialize(testTracks);
        trackLoader.loadTrack(testTracks.get(0));
        
        await().atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> assertThat(trackLoader.getPreloadedTracks()).isNotEmpty());
        
        trackLoader.unloadAll();
        
        assertThat(trackLoader.getPreloadedTracks()).isEmpty();
    }

    @Test
    void shouldReportResourceSavingModeEnabled() {
        assertThat(trackLoader.isResourceSavingMode()).isTrue();
    }

    @Test
    void shouldToggleResourceSavingMode() {
        trackLoader.setResourceSavingMode(false);
        assertThat(trackLoader.isResourceSavingMode()).isFalse();
        
        trackLoader.setResourceSavingMode(true);
        assertThat(trackLoader.isResourceSavingMode()).isTrue();
    }

    @Test
    void shouldNotifyListenersOfPreloadChanges() {
        CopyOnWriteArrayList<Track> notifiedTracks = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<String> events = new CopyOnWriteArrayList<>();
        
        trackLoader.addLoadListener(new TrackLoader.TrackLoadListener() {
            @Override
            public void onTrackPreloaded(Track track) {
                notifiedTracks.add(track);
                events.add("preloaded:" + track.getTitle());
            }

            @Override
            public void onTrackUnloaded(Track track) {
                events.add("unloaded:" + track.getTitle());
            }

            @Override
            public void onPreloadQueueChanged(List<Track> preloadedTracks) {
                events.add("queueChanged:" + preloadedTracks.size());
            }

            @Override
            public void onLoadingError(Track track, String error) {
                events.add("error:" + error);
            }
        });
        
        trackLoader.initialize(testTracks);
        trackLoader.loadTrack(testTracks.get(0));
        
        await().atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                assertThat(notifiedTracks).isNotEmpty();
                assertThat(events).isNotEmpty();
            });
    }

    @Test
    void shouldCloseProperly() {
        trackLoader.initialize(testTracks);
        trackLoader.close();
        
        assertThat(trackLoader.getPreloadedTracks()).isEmpty();
    }
}
