package com.mycompany.cardcreator.util;

import javax.sound.sampled.*;
import java.io.InputStream;

public class SoundPlayer {

    public static void playSound(String path) {
        try {
            InputStream is = SoundPlayer.class.getClassLoader().getResourceAsStream(path);

            if (is == null) {
                System.out.println("❌ Sound not found in resources: " + path);
                return;
            }

            AudioInputStream audio =
                AudioSystem.getAudioInputStream(new java.io.BufferedInputStream(is));

            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();

        } catch (Exception e) {
            System.out.println("❌ Sound error: " + e.getMessage());
        }
    }
}