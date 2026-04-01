package com.musicplayer.application;

import com.musicplayer.application.controller.PlayerController;
import com.musicplayer.application.service.AudioService;
import com.musicplayer.application.service.FileService;
import com.musicplayer.application.service.PlaylistService;
import com.musicplayer.domain.model.PlaybackState;
import com.musicplayer.domain.model.RepeatMode;
import com.musicplayer.domain.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PlayerControllerTest {

    private AudioService mockAudioService;
    private FileService mockFileService;
    private PlaylistService playlistService;
    private PlayerController controller;

    @BeforeEach
    void setUp() {
        mockAudioService = mock(AudioService.class);
        mockFileService = mock(FileService.class);
        playlistService = new PlaylistService();
        controller = new PlayerController(mockAudioService, playlistService, mockFileService);
    }

    @Test
    void shouldReturnIdleStateInitially() {
        assertThat(controller.getPlaybackState()).isEqualTo(PlaybackState.IDLE);
    }

    @Test
    void shouldReturnCorrectRepeatMode() {
        assertThat(controller.getRepeatMode()).isEqualTo(RepeatMode.OFF);
    }

    @Test
    void shouldToggleRepeatMode() {
        controller.toggleRepeat();
        assertThat(controller.getRepeatMode()).isEqualTo(RepeatMode.ALL);

        controller.toggleRepeat();
        assertThat(controller.getRepeatMode()).isEqualTo(RepeatMode.ONE);

        controller.toggleRepeat();
        assertThat(controller.getRepeatMode()).isEqualTo(RepeatMode.OFF);
    }

    @Test
    void shouldToggleShuffle() {
        assertThat(controller.isShuffled()).isFalse();

        controller.toggleShuffle();
        assertThat(controller.isShuffled()).isTrue();

        controller.toggleShuffle();
        assertThat(controller.isShuffled()).isFalse();
    }

    @Test
    void shouldReturnEmptyPlaylistInitially() {
        List<Track> playlist = controller.getPlaylist();
        assertThat(playlist).isEmpty();
    }

    @Test
    void shouldReturnNullCurrentTrackInitially() {
        assertThat(controller.getCurrentTrack()).isNull();
    }

    @Test
    void shouldSetVolume() {
        controller.setVolume(0.5);
        verify(mockAudioService).setVolume(0.5);
    }

    @Test
    void shouldClampVolumeToMaximum() {
        controller.setVolume(1.5);
        verify(mockAudioService).setVolume(1.0);
    }

    @Test
    void shouldClampVolumeToMinimum() {
        controller.setVolume(-0.5);
        verify(mockAudioService).setVolume(0.0);
    }

    @Test
    void shouldSeekToPosition() {
        Duration position = Duration.ofSeconds(30);
        controller.seek(position);
        verify(mockAudioService).seek(position);
    }

    @Test
    void shouldPause() {
        controller.pause();
        verify(mockAudioService).pause();
    }

    @Test
    void shouldResume() {
        controller.resume();
        verify(mockAudioService).resume();
    }

    @Test
    void shouldStop() {
        controller.stop();
        verify(mockAudioService).stop();
    }

    @Test
    void shouldReturnCorrectCurrentIndex() {
        assertThat(controller.getCurrentIndex()).isEqualTo(-1);
    }

    @Test
    void shouldReturnIsPlayingFalseInitially() {
        assertThat(controller.isPlaying()).isFalse();
    }

    @Test
    void shouldReturnDurationAsNullInitially() {
        when(mockAudioService.getDuration()).thenReturn(null);
        
        Duration duration = controller.getDuration();
        
        assertThat(duration).isNull();
    }

    @Test
    void shouldReturnCurrentPositionAsZeroInitially() {
        when(mockAudioService.getCurrentPosition()).thenReturn(Duration.ZERO);
        
        Duration position = controller.getCurrentPosition();
        
        assertThat(position).isEqualTo(Duration.ZERO);
    }

    @Test
    void shouldGetVolume() {
        when(mockAudioService.getVolume()).thenReturn(0.75);

        double volume = controller.getVolume();

        assertThat(volume).isEqualTo(0.75);
    }

    @Test
    void shouldIgnoreInvalidTrackIndex() {
        Track track = new Track("/music/1.mp3");
        playlistService.addTrack(track);

        controller.playTrack(5);

        assertThat(controller.getCurrentTrack()).isEqualTo(track);
    }

    @Test
    void shouldReturnCorrectPlaylistSize() {
        playlistService.addTrack(new Track("/music/1.mp3"));
        playlistService.addTrack(new Track("/music/2.mp3"));

        assertThat(controller.getPlaylist()).hasSize(2);
    }

}
