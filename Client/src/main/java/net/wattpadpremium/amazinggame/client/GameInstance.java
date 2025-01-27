package net.wattpadpremium.amazinggame.client;

import lombok.Getter;
import lombok.Setter;


import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.net.URL;

@Getter
public class GameInstance {

    private final Profile profile;

    @Setter
    private GameScene mazeGame = null;

    public GameInstance(Profile profile) {
        this.profile = profile;
        new GameMenu(this).setVisible(true);
        playMP3FromResources("game_song.wav");
    }

    public static void main(String[] args) {
        new GameInstance(new Profile());
    }

    private void playMP3FromResources(String filename) {
        try {
            URL songUrl = getClass().getClassLoader().getResource(filename);
            if (songUrl == null) {
                System.out.println("MP3 file not found: " + filename);
                return;
            }

            System.out.println(songUrl.getPath());

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(songUrl.getPath()).getAbsoluteFile());

            Clip clip = AudioSystem.getClip();

            clip.open(audioInputStream);

            clip.loop(Clip.LOOP_CONTINUOUSLY);

            clip.start();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
