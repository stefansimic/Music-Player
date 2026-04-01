package com.musicplayer.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Represents a playlist (queue) of tracks.
 * 
 * Provides methods for managing tracks, navigation, and playback modes.
 */
public class Playlist implements Iterable<Track> {
    
    private final String name;
    private final List<Track> tracks;
    private int currentIndex;
    private RepeatMode repeatMode;
    private boolean shuffled;

    /**
     * Creates an empty playlist with the given name.
     *
     * @param name the playlist name
     * @throws IllegalArgumentException if name is null
     */
    public Playlist(String name) {
        this.name = Objects.requireNonNull(name, "Playlist name cannot be null");
        this.tracks = new ArrayList<>();
        this.currentIndex = -1;
        this.repeatMode = RepeatMode.OFF;
        this.shuffled = false;
    }

    /**
     * Creates a playlist with the given name and initial tracks.
     *
     * @param name   the playlist name
     * @param tracks the initial tracks
     * @throws IllegalArgumentException if name or tracks is null
     */
    public Playlist(String name, List<Track> tracks) {
        this.name = Objects.requireNonNull(name, "Playlist name cannot be null");
        this.tracks = new ArrayList<>(Objects.requireNonNull(tracks, "Tracks list cannot be null"));
        this.currentIndex = tracks.isEmpty() ? -1 : 0;
        this.repeatMode = RepeatMode.OFF;
        this.shuffled = false;
    }

    /**
     * Adds a track to the end of the playlist.
     *
     * @param track the track to add
     * @throws IllegalArgumentException if track is null
     */
    public void addTrack(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Track cannot be null");
        }
        tracks.add(track);
        if (currentIndex == -1) {
            currentIndex = 0;
        }
    }

    /**
     * Removes the track at the specified index.
     *
     * @param index the index of the track to remove
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public void removeTrack(int index) {
        validateIndex(index);
        tracks.remove(index);
        adjustCurrentIndexAfterRemoval(index);
    }

    /**
     * Moves a track from one position to another.
     *
     * @param fromIndex the current index of the track
     * @param toIndex   the new index for the track
     * @throws IndexOutOfBoundsException if either index is invalid
     */
    public void moveTrack(int fromIndex, int toIndex) {
        validateIndex(fromIndex);
        validateIndex(toIndex);
        if (fromIndex == toIndex) {
            return;
        }
        Track track = tracks.remove(fromIndex);
        tracks.add(toIndex, track);
        updateCurrentIndexAfterMove(fromIndex, toIndex);
    }

    /**
     * Gets the track at the specified index.
     *
     * @param index the track index
     * @return the track at that index
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public Track getTrack(int index) {
        validateIndex(index);
        return tracks.get(index);
    }

    /**
     * Returns the currently selected track.
     *
     * @return the current track, or null if playlist is empty
     */
    public Track getCurrentTrack() {
        if (currentIndex < 0 || currentIndex >= tracks.size()) {
            return null;
        }
        return tracks.get(currentIndex);
    }

    /**
     * Returns the current track index.
     *
     * @return the index of the current track, or -1 if no track is selected
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Sets the current track index.
     *
     * @param index the new index (-1 to clear selection, 0 to size-1 for valid index)
     * @throws IndexOutOfBoundsException if index is outside valid range
     */
    public void setCurrentIndex(int index) {
        if (index < -1 || index >= tracks.size()) {
            throw new IndexOutOfBoundsException("Invalid track index: " + index);
        }
        this.currentIndex = index;
    }

    /**
     * Advances to and returns the next track.
     * 
     * Behavior depends on repeat mode:
     * - OFF: returns null when at last track
     * - ALL: loops back to first track
     * - ONE: returns current track (handled separately)
     *
     * @return the next track, or null if at end with RepeatMode.OFF
     */
    public Track getNextTrack() {
        if (tracks.isEmpty()) {
            return null;
        }
        if (repeatMode == RepeatMode.ONE) {
            return getCurrentTrack();
        }
        currentIndex = (currentIndex + 1) % tracks.size();
        return tracks.get(currentIndex);
    }

    /**
     * Returns the next track without changing current position.
     * Useful for preview functionality.
     *
     * @return the next track, or null if at end with RepeatMode.OFF
     */
    public Track peekNextTrack() {
        if (tracks.isEmpty() || (currentIndex >= tracks.size() - 1 && repeatMode == RepeatMode.OFF)) {
            return null;
        }
        int nextIndex = (currentIndex + 1) % tracks.size();
        return tracks.get(nextIndex);
    }

    /**
     * Goes back to and returns the previous track.
     * Loops to last track if at first track.
     *
     * @return the previous track, or null if playlist is empty
     */
    public Track getPreviousTrack() {
        if (tracks.isEmpty()) {
            return null;
        }
        currentIndex = (currentIndex - 1 + tracks.size()) % tracks.size();
        return tracks.get(currentIndex);
    }

    /**
     * Randomizes the order of tracks in the playlist.
     * Resets current index to 0.
     */
    public void shuffle() {
        Collections.shuffle(tracks);
        shuffled = true;
        currentIndex = tracks.isEmpty() ? -1 : 0;
    }

    /**
     * Restores the original order of tracks.
     */
    public void unshuffle() {
        Collections.sort(tracks, (t1, t2) -> {
            String name1 = t1.getPath();
            String name2 = t2.getPath();
            return name1.compareToIgnoreCase(name2);
        });
        shuffled = false;
        currentIndex = tracks.isEmpty() ? -1 : 0;
    }

    /**
     * Returns whether the playlist is currently shuffled.
     *
     * @return true if shuffled
     */
    public boolean isShuffled() {
        return shuffled;
    }

    /**
     * Clears all tracks from the playlist.
     */
    public void clear() {
        tracks.clear();
        currentIndex = -1;
    }

    /**
     * Returns the number of tracks in the playlist.
     *
     * @return the track count
     */
    public int size() {
        return tracks.size();
    }

    /**
     * Returns whether the playlist is empty.
     *
     * @return true if no tracks
     */
    public boolean isEmpty() {
        return tracks.isEmpty();
    }

    /**
     * Returns the playlist name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the current repeat mode.
     *
     * @return the repeat mode
     */
    public RepeatMode getRepeatMode() {
        return repeatMode;
    }

    /**
     * Sets the repeat mode.
     *
     * @param repeatMode the new repeat mode
     */
    public void setRepeatMode(RepeatMode repeatMode) {
        this.repeatMode = Objects.requireNonNull(repeatMode, "Repeat mode cannot be null");
    }

    /**
     * Cycles to the next repeat mode.
     *
     * @return the new repeat mode
     */
    public RepeatMode nextRepeatMode() {
        this.repeatMode = repeatMode.next();
        return this.repeatMode;
    }

    /**
     * Returns an unmodifiable list of all tracks.
     *
     * @return immutable list of tracks
     */
    public List<Track> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    @Override
    public Iterator<Track> iterator() {
        return tracks.iterator();
    }

    private void validateIndex(int index) {
        if (index < 0 || index >= tracks.size()) {
            throw new IndexOutOfBoundsException("Invalid track index: " + index);
        }
    }

    private void adjustCurrentIndexAfterRemoval(int removedIndex) {
        if (tracks.isEmpty()) {
            currentIndex = -1;
        } else if (removedIndex < currentIndex) {
            currentIndex--;
        } else if (removedIndex == currentIndex) {
            currentIndex = Math.min(currentIndex, tracks.size() - 1);
        }
    }

    private void updateCurrentIndexAfterMove(int fromIndex, int toIndex) {
        if (currentIndex == fromIndex) {
            currentIndex = toIndex;
        } else if (fromIndex < currentIndex && toIndex >= currentIndex) {
            currentIndex--;
        } else if (fromIndex > currentIndex && toIndex <= currentIndex) {
            currentIndex++;
        }
    }
}
