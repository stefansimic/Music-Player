package com.musicplayer.ui.javafx;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class IconFactory {
    
    private static final Color ICON_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = Color.rgb(102, 126, 234);
    
    public static Node createPlayIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M8 5v14l11-7z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createPauseIcon() {
        Group group = new Group();
        Rectangle r1 = new Rectangle(6, 4, 4, 16);
        Rectangle r2 = new Rectangle(14, 4, 4, 16);
        r1.setFill(ICON_COLOR);
        r2.setFill(ICON_COLOR);
        group.getChildren().addAll(r1, r2);
        return group;
    }
    
    public static Node createPreviousIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M6 6h2v12H6zM16 6l-10 6 10 6V6z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createNextIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M16 6h2v12h-2zM6 6l10 6-10 6V6z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createShuffleIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M10.59 9.17L5.41 4 4 5.41l5.17 5.17 1.42-1.41zM14.5 4l2.04 2.04L4 18.59 5.41 20 17.96 7.46 20 9.5V4h-5.5zm.33 9.41l-1.41 1.41 3.13 3.13L14.5 20H20v-5.5l-2.04 2.04-3.13-3.13z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createRepeatIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M7 7h10v3l4-4-4-4v3H5v6h2V7zm10 10H7v-3l-4 4 4 4v-3h12v-6h-2v4z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createRepeatOneIcon() {
        Group group = new Group();
        SVGPath repeat = new SVGPath();
        repeat.setContent("M7 7h10v3l4-4-4-4v3H5v6h2V7zm10 10H7v-3l-4 4 4 4v-3h12v-6h-2v4z");
        repeat.setFill(ICON_COLOR);
        
        Text one = new Text("1");
        one.setFill(ACCENT_COLOR);
        one.setFont(Font.font(10));
        one.setX(17);
        one.setY(15);
        
        group.getChildren().addAll(repeat, one);
        return group;
    }
    
    public static Node createVolumeIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createVolumeMuteIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createMusicNoteIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createAlbumIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 14.5c-2.49 0-4.5-2.01-4.5-4.5S9.51 7.5 12 7.5s4.5 2.01 4.5 4.5-2.01 4.5-4.5 4.5zm0-5.5c-.55 0-1 .45-1 1s.45 1 1 1 1-.45 1-1-.45-1-1-1z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createArtistIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createPlaylistIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M15 6H3v2h12V6zm0 4H3v2h12v-2zM3 16h8v-2H3v2zM17 6v8.18c-.31-.11-.65-.18-1-.18-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3V8h3V6h-5z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createQueueIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M15 6H3v2h12V6zm0 4H3v2h12v-2zM3 16h8v-2H3v2zM17 6v8.18c-.31-.11-.65-.18-1-.18-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3V8h3V6h-5z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createFolderIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createHomeIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createSearchIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createSettingsIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M19.14 12.94c.04-.31.06-.63.06-.94 0-.31-.02-.63-.06-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.04.31-.06.63-.06.94s.02.63.06.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createChevronLeftIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createChevronRightIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createMenuIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createCloseIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createAddIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createHeartIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Node createHeartOutlineIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z");
        path.setFill(ICON_COLOR);
        return path;
    }
    
    public static Button createIconButton(Node icon) {
        Button button = new Button();
        button.setGraphic(icon);
        button.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 8;");
        return button;
    }
    
    public static Button createGlassButton(String text, Node icon) {
        Button button = new Button(text);
        button.setGraphic(icon);
        button.getStyleClass().add("button-glass");
        return button;
    }
}
