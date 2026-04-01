package com.musicplayer.domain;

import com.musicplayer.domain.model.RepeatMode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RepeatModeTest {

    @Test
    void shouldHaveThreeRepeatModes() {
        assertThat(RepeatMode.values()).hasSize(3);
    }

    @Test
    void shouldCycleFromOffToAll() {
        assertThat(RepeatMode.OFF.next()).isEqualTo(RepeatMode.ALL);
    }

    @Test
    void shouldCycleFromAllToOne() {
        assertThat(RepeatMode.ALL.next()).isEqualTo(RepeatMode.ONE);
    }

    @Test
    void shouldCycleFromOneToOff() {
        assertThat(RepeatMode.ONE.next()).isEqualTo(RepeatMode.OFF);
    }
}
