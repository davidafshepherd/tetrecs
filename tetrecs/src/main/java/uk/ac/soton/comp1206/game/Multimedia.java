package uk.ac.soton.comp1206.game;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *The Multimedia Class. Plays audio and looped music.
 */
public class Multimedia {

    private static final Logger logger = LogManager.getLogger(Multimedia.class);

    /**
     * Audio Player
     */
    private static MediaPlayer audioPlayer;

    /**
     * Music Player
     */
    private static MediaPlayer musicPlayer;

    /**
     *Plays an audio
     * @param file name of audio file to play
     */
    public static void playAudio(String file) {
        //Gets audio file
        var toPlay = Multimedia.class.getResource("/" + file).toExternalForm();

        //Tries to play audio file
        try {
            var play = new Media(toPlay);
            audioPlayer = new MediaPlayer(play);
            audioPlayer.play();
            logger.info("Playing audio: " + toPlay);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to play audio file");
        }
    }

    /**
     *Plays a music on a loop
     * @param file name of music file to be played on a loop
     */
    public static void playMusic(String file) {
        //Gets music file
        var toPlay = Multimedia.class.getResource("/" + file).toExternalForm();

        //Tries to play music file
        try {
            var play = new Media(toPlay);
            musicPlayer = new MediaPlayer(play);
            musicPlayer.play();
            logger.info("Playing audio: " + toPlay);
            //Sets the music file to be played on a loop
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to play audio file");
        }
    }

    /**
     * Gets the music player
     * @return the music player
     */
    public static MediaPlayer getMusicPlayer() {
        return musicPlayer;
    }
}
