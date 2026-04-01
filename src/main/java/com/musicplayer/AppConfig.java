package com.musicplayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Application configuration manager.
 * 
 * Loads configuration from properties files with the following precedence:
 * 1. System property (-Dconfig.file=/path/to/config.properties)
 * 2. Current directory (./config.properties)
 * 3. User home (~/.musicplayer/config.properties)
 * 4. Classpath (/config.properties - defaults)
 */
public class AppConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    
    private static final String CONFIG_FILE_PROPERTY = "config.file";
    private static final String DEFAULT_CONFIG = "/config.properties";
    
    private static final Properties properties = new Properties();
    
    private static double volumeDefault = 0.7;
    private static boolean recursiveScanning = true;
    private static double windowWidth = 800;
    private static double windowHeight = 600;
    private static int progressUpdateMs = 100;

    static {
        load();
    }

    /**
     * Loads configuration from available sources.
     */
    public static void load() {
        loadFromClasspath();
        loadFromFileSystem();
        loadFromSystemProperty();
        parseProperties();
    }

    private static void loadFromClasspath() {
        try (InputStream input = AppConfig.class.getResourceAsStream(DEFAULT_CONFIG)) {
            if (input != null) {
                properties.load(input);
                logger.info("Loaded configuration from classpath");
            }
        } catch (IOException e) {
            logger.debug("No classpath configuration found");
        }
    }

    private static void loadFromFileSystem() {
        Path userHomeConfig = Path.of(System.getProperty("user.home"), ".musicplayer", "config.properties");
        if (Files.exists(userHomeConfig)) {
            try (InputStream input = Files.newInputStream(userHomeConfig)) {
                properties.load(input);
                logger.info("Loaded configuration from user home: {}", userHomeConfig);
            } catch (IOException e) {
                logger.warn("Failed to load config from user home", e);
            }
        }
        
        Path currentDirConfig = Path.of("config.properties");
        if (Files.exists(currentDirConfig)) {
            try (InputStream input = Files.newInputStream(currentDirConfig)) {
                properties.load(input);
                logger.info("Loaded configuration from current directory");
            } catch (IOException e) {
                logger.warn("Failed to load config from current directory", e);
            }
        }
    }

    private static void loadFromSystemProperty() {
        String configPath = System.getProperty(CONFIG_FILE_PROPERTY);
        if (configPath != null && !configPath.isBlank()) {
            Path customConfig = Path.of(configPath);
            if (Files.exists(customConfig)) {
                try (InputStream input = Files.newInputStream(customConfig)) {
                    properties.load(input);
                    logger.info("Loaded configuration from system property: {}", customConfig);
                } catch (IOException e) {
                    logger.warn("Failed to load custom config", e);
                }
            }
        }
    }

    private static void parseProperties() {
        volumeDefault = getDouble("audio.volume.default", 0.7);
        recursiveScanning = getBoolean("scan.recursive", true);
        windowWidth = getDouble("ui.window.width", 800);
        windowHeight = getDouble("ui.window.height", 600);
        progressUpdateMs = getInt("ui.progress-update-ms", 100);
    }

    private static double getDouble(String key, double defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid double value for {}: {}", key, value);
            }
        }
        return defaultValue;
    }

    private static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    private static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid int value for {}: {}", key, value);
            }
        }
        return defaultValue;
    }

    public static double getVolumeDefault() {
        return volumeDefault;
    }

    public static boolean isRecursiveScanning() {
        return recursiveScanning;
    }

    public static double getWindowWidth() {
        return windowWidth;
    }

    public static double getWindowHeight() {
        return windowHeight;
    }

    public static int getProgressUpdateMs() {
        return progressUpdateMs;
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
