package seng302.Common.Utils;

import javafx.animation.Transition;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import seng302.Client.ClientMain;

import javax.sound.sampled.*;
import java.io.IOException;

import static seng302.Client.ClientMain.mediaPlayers;

public class Sound {

    private boolean loop = false;
    private boolean fade = false;
    private Media media;

    private MediaPlayer mediaPlayer;

    /**
     * Class starts playing the sound on initialisation.
     * @param media media that will be played
     * @param loop boolean that decides if music will loop
     * @param fade boolean that decides if music will fade in
     * @throws UnsupportedAudioFileException not valid data
     * @throws IOException when file cannot be found
     * @throws LineUnavailableException the line is unavailable.
     */
    public Sound(Media media, boolean loop, boolean fade) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        this.media = media;
        this.loop = loop;
        this.fade = fade;
        playSound(media);
    }

    /**
     * creates a mediaPlayer from a media file and starts it. It will dispose of the mediaplayer when sound finishes.
     * @param media media file that is to be played
     * @throws IOException  when file cannot be found
     * @throws UnsupportedAudioFileException not valid data
     * @throws LineUnavailableException the line is unavailable.
     */
    private void playSound(Media media) throws IOException, UnsupportedAudioFileException, LineUnavailableException {

        mediaPlayer = new MediaPlayer(media);

        mediaPlayers.add(mediaPlayer);
        if (loop) {
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        } else {
            mediaPlayer.setOnEndOfMedia(() ->
            {
                mediaPlayers.remove(mediaPlayer);
                mediaPlayer.dispose(); //There is a limit of 30 mediaPlayers at once so MUST dispose
            });
        }

        if(ClientMain.isMuted()){
            mediaPlayer.play();
            mediaPlayer.setMute(true);

        } else if(fade) {
            mediaPlayer.setVolume(0.0);
            mediaPlayer.play();
            Transition fadeIn = new Transition() {
                {
                    setCycleDuration(Duration.seconds(2));
                }

                @Override
                protected void interpolate(double frac) {
                    mediaPlayer.setVolume(frac);
                }
            };
            fadeIn.play();
        } else {
            mediaPlayer.setVolume(1.0);
            mediaPlayer.play();
        }
    }

    public Duration getDuration() {
        return media.getDuration();
    }
}

