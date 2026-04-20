package com.mycompany.cardcreator.util;

import javax.sound.sampled.*;
import java.io.InputStream;

public class SoundPlayer {

    // classpath location of the standard ui click sound
    public static final String CLICK = "Sounds/button-10.wav";

    // shorthand for the standard button click sound used across the ui
    public static void playClick() {
        playSound(CLICK);
    }

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