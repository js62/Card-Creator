package com.mycompany.cardcreator.util;

import javax.sound.sampled.*;
import java.io.InputStream;

/**
 * Plays short UI sound clips from the classpath.
 *
 * Every sound file is expected to live under the Sounds folder on the
 * classpath. Missing files and playback errors are printed to stdout and
 * the call returns without throwing, so a broken sound never stops the
 * UI from working.
 */
public class SoundPlayer {

    /** Classpath location of the standard button click sound. */
    public static final String CLICK = "Sounds/button-10.wav";

    /**
     * Plays the standard button click sound.
     *
     * Shorthand for playSound(CLICK), used from every clickable control
     * so the whole UI sounds the same.
     */
    public static void playClick() {
        playSound(CLICK);
    }

    /**
     * Plays the sound at the given classpath path.
     *
     * If the resource cannot be found or the audio cannot be decoded, a
     * message is printed and the call returns without throwing.
     *
     * @param path classpath location of the sound file, for example
     *             "Sounds/button-10.wav"
     */
    public static void playSound(String path) {
        try {
            InputStream is = SoundPlayer.class.getClassLoader().getResourceAsStream(path);

            if (is == null) {
                System.out.println("Sound not found in resources: " + path);
                return;
            }

            AudioInputStream audio =
                AudioSystem.getAudioInputStream(new java.io.BufferedInputStream(is));

            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();

        } catch (Exception e) {
            System.out.println("Sound error: " + e.getMessage());
        }
    }
}
