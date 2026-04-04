package com.musicplayer;

import com.musicplayer.application.controller.PlayerController;
import com.musicplayer.application.service.AudioService;
import com.musicplayer.application.service.FileService;
import com.musicplayer.application.service.LibraryService;
import com.musicplayer.application.service.PlaylistService;
import com.musicplayer.domain.contract.AudioPlayer;
import com.musicplayer.domain.contract.FileScanner;
import com.musicplayer.domain.contract.LibraryRepository;
import com.musicplayer.domain.contract.MetadataReader;
import com.musicplayer.domain.contract.TrackLoader;
import com.musicplayer.infrastructure.audio.EagerTrackLoader;
import com.musicplayer.infrastructure.audio.JavaFxAudioPlayer;
import com.musicplayer.infrastructure.audio.LazyTrackLoader;
import com.musicplayer.infrastructure.filesystem.NioFileScanner;
import com.musicplayer.infrastructure.metadata.JAudioTaggerMetadataReader;
import com.musicplayer.infrastructure.persistence.JsonLibraryRepository;
import com.musicplayer.ui.javafx.MusicPlayerUI;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Music Player application.
 */
public class Main extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    private PlayerController controller;

    public static void main(String[] args) {
        logger.info("Starting Music Player v2.0.0");
        AppConfig.load();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            initializeController();
            
            MusicPlayerUI ui = new MusicPlayerUI(controller);
            
            Scene scene = new Scene(ui, AppConfig.getWindowWidth(), AppConfig.getWindowHeight());
            
            primaryStage.setTitle("Music Player");
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(event -> {
                controller.dispose();
                logger.info("Music Player closed");
            });
            
            primaryStage.show();
            
            logger.info("Music Player started successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start Music Player", e);
            System.exit(1);
        }
    }

    private void initializeController() {
        AudioPlayer audioPlayer = new JavaFxAudioPlayer();
        FileScanner fileScanner = new NioFileScanner();
        MetadataReader metadataReader = new JAudioTaggerMetadataReader();
        LibraryRepository libraryRepository = new JsonLibraryRepository();
        
        AudioService audioService = new AudioService(audioPlayer);
        PlaylistService playlistService = new PlaylistService();
        FileService fileService = new FileService(
            fileScanner, 
            metadataReader, 
            AppConfig.isRecursiveScanning()
        );
        LibraryService libraryService = new LibraryService(
            libraryRepository,
            fileScanner,
            metadataReader
        );
        
        TrackLoader trackLoader = createTrackLoader(audioPlayer);
        
        audioService.setVolume(AppConfig.getVolumeDefault());
        
        controller = new PlayerController(audioService, playlistService, fileService, libraryService, trackLoader);
    }
    
    private TrackLoader createTrackLoader(AudioPlayer audioPlayer) {
        if (AppConfig.isResourceSavingMode()) {
            return new LazyTrackLoader(audioPlayer, AppConfig.getPreloadTrackCount());
        }
        return new EagerTrackLoader(audioPlayer);
    }

    @Override
    public void stop() throws Exception {
        if (controller != null) {
            controller.dispose();
        }
        super.stop();
    }
}
