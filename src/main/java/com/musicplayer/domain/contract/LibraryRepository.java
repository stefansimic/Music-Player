package com.musicplayer.domain.contract;

import com.musicplayer.domain.exception.LibraryStorageException;
import com.musicplayer.domain.model.Library;

/**
 * Contract for persisting and loading the music library.
 */
public interface LibraryRepository {

    /**
     * Saves the library to persistent storage.
     *
     * @param library the library to save
     * @throws LibraryStorageException if the save operation fails
     */
    void save(Library library) throws LibraryStorageException;

    /**
     * Loads the library from persistent storage.
     *
     * @return the loaded library, or a new empty library if none exists
     * @throws LibraryStorageException if the load operation fails
     */
    Library load() throws LibraryStorageException;

    /**
     * Checks if a saved library exists.
     *
     * @return true if a library file exists
     */
    boolean exists();

    /**
     * Deletes the saved library.
     *
     * @throws LibraryStorageException if the delete operation fails
     */
    void delete() throws LibraryStorageException;
}
