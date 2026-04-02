package com.musicplayer.ui.javafx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

public class ArtworkPanel extends VBox {
    
    private static final Logger logger = LoggerFactory.getLogger(ArtworkPanel.class);
    
    private static final int DEFAULT_SIZE = 100;
    
    private final ImageView imageView;
    private final Image placeholderImage;
    
    public ArtworkPanel() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(0));
        setStyle(
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;"
        );
        
        imageView = new ImageView();
        imageView.setFitWidth(DEFAULT_SIZE);
        imageView.setFitHeight(DEFAULT_SIZE);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setStyle(
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;"
        );
        
        placeholderImage = createPlaceholderImage();
        imageView.setImage(placeholderImage);
        
        setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        
        getChildren().add(imageView);
    }
    
    public ArtworkPanel(int size) {
        this();
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
    }
    
    public void setArtwork(byte[] artworkBytes) {
        if (artworkBytes != null && artworkBytes.length > 0) {
            try {
                Image image = new Image(new ByteArrayInputStream(artworkBytes));
                imageView.setImage(image);
                logger.debug("Artwork set successfully");
            } catch (Exception e) {
                logger.warn("Failed to load artwork image", e);
                setPlaceholder();
            }
        } else {
            setPlaceholder();
        }
    }
    
    public void setPlaceholder() {
        imageView.setImage(placeholderImage);
    }
    
    public void clear() {
        setPlaceholder();
    }
    
    private Image createPlaceholderImage() {
        String svgData = """
            <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100">
              <defs>
                <linearGradient id="grad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style="stop-color:#667eea"/>
                  <stop offset="100%" style="stop-color:#764ba2"/>
                </linearGradient>
              </defs>
              <rect width="100" height="100" fill="url(#grad)" rx="8"/>
              <circle cx="50" cy="36" r="16" fill="rgba(255,255,255,0.3)"/>
              <path d="M32 65 Q50 50 68 65 L68 80 Q50 68 32 80 Z" fill="rgba(255,255,255,0.3)"/>
            </svg>
            """;
        
        return new Image("data:image/svg+xml;base64," + 
                java.util.Base64.getEncoder().encodeToString(svgData.getBytes()));
    }
}
