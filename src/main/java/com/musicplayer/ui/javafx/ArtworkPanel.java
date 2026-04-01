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

/**
 * Panel displaying the album artwork of the currently playing track.
 */
public class ArtworkPanel extends VBox {
    
    private static final Logger logger = LoggerFactory.getLogger(ArtworkPanel.class);
    
    private static final int DEFAULT_SIZE = 150;
    
    private final ImageView imageView;
    private final Image placeholderImage;
    
    public ArtworkPanel() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(10));
        
        imageView = new ImageView();
        imageView.setFitWidth(DEFAULT_SIZE);
        imageView.setFitHeight(DEFAULT_SIZE);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        
        placeholderImage = createPlaceholderImage();
        imageView.setImage(placeholderImage);
        
        setStyle("-fx-background-color: #2a2a2a;");
        setBackground(new Background(new BackgroundFill(Color.rgb(42, 42, 42), CornerRadii.EMPTY, Insets.EMPTY)));
        
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
            <svg xmlns="http://www.w3.org/2000/svg" width="150" height="150" viewBox="0 0 150 150">
              <rect width="150" height="150" fill="#3a3a3a"/>
              <circle cx="75" cy="60" r="30" fill="#555"/>
              <path d="M45 100 Q75 80 105 100 L105 130 Q75 115 45 130 Z" fill="#555"/>
            </svg>
            """;
        
        return new Image("data:image/svg+xml;base64," + 
                java.util.Base64.getEncoder().encodeToString(svgData.getBytes()));
    }
}
