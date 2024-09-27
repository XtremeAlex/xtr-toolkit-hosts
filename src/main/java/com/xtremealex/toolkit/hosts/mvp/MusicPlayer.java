package com.xtremealex.toolkit.hosts.mvp;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;


//VOGLIO SPOSTARE TUTTA LA LOGICA DELLA MUSICA QUI ... WORKING-PROGRESS
public class MusicPlayer {

    private MediaPlayer mediaPlayer;
    private final double maxVolume = 0.1;
    private Runnable onTransitionComplete;

    public MusicPlayer(String musicPath) {
        initializeMediaPlayer(musicPath);
    }

    private void initializeMediaPlayer(String musicPath) {
        URL musicURL = getClass().getResource(musicPath);
        if (musicURL != null) {
            Media media = new Media(musicURL.toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            mediaPlayer.setOnReady(() -> {
                System.out.println("MediaPlayer is ready");
            });

            mediaPlayer.setOnError(() -> {
                System.err.println("Error occurred while loading the media: " + mediaPlayer.getError());
            });

        } else {
            System.err.println("Errore: file audio non trovato!");
        }
    }

    public void setOnTransitionComplete(Runnable callback) {
        this.onTransitionComplete = callback;
    }

    public void playMusic() {
        if (mediaPlayer != null) {
            MediaPlayer.Status status = mediaPlayer.getStatus();
            System.out.println("Status before play: " + status);

            if (status == MediaPlayer.Status.READY || status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.STOPPED) {
                fadeInMusic();
                System.out.println("Playing music");
            } else if (status == MediaPlayer.Status.UNKNOWN) {
                mediaPlayer.setOnReady(() -> fadeInMusic());
                System.out.println("Waiting for MediaPlayer to be ready...");
            }
        } else {
            System.out.println("MediaPlayer is null");
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            fadeOutMusic();
        }
    }

    private void fadeInMusic() {
        mediaPlayer.play();
        mediaPlayer.setVolume(0.0);

        Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(mediaPlayer.volumeProperty(), 0.0)),
                new KeyFrame(Duration.seconds(2), new KeyValue(mediaPlayer.volumeProperty(), maxVolume))
        );

        fadeIn.setOnFinished(event -> {
            if (onTransitionComplete != null) {
                onTransitionComplete.run();
            }
        });

        fadeIn.play();
    }

    private void fadeOutMusic() {
        if (onTransitionComplete != null) {
            onTransitionComplete.run();
        }

        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(mediaPlayer.volumeProperty(), maxVolume)),
                new KeyFrame(Duration.seconds(2), new KeyValue(mediaPlayer.volumeProperty(), 0.0))
        );

        fadeOut.setOnFinished(event -> {
            mediaPlayer.pause();
            if (onTransitionComplete != null) {
                onTransitionComplete.run();
            }
        });

        fadeOut.play();
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public boolean isPaused() {
        return mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED;
    }

    public boolean isReady() {
        return mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.READY;
    }
}