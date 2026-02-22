package net.wattpadpremium.amazinggame.client;

import lombok.Getter;


import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.UUID;

@Getter
public class Game {

    private final GameVariables gameVariables;

    private final GameMenu mainMenu;

    private final MultiplayerMenu multiplayerMenu;

    private final PlayScene playScene;

    public Game(GameVariables gameVariables) {
        this.gameVariables = gameVariables;
        this.mainMenu = new GameMenu(this);
        this.multiplayerMenu = new MultiplayerMenu(this);
        this.playScene = new PlayScene(this);

        mainMenu.setVisible(true);
        //playMP3FromResources("game_song.wav");
    }

    static void main(String[] args) {
        GameVariables gameVariables = new GameVariables();
        if (args.length != 0){
            String userToken = args[0];
            //todo implement username retrieval
            gameVariables.setUsername("Online");
            gameVariables.setUserToken(userToken);
            gameVariables.setOnlineMode(true);
            gameVariables.setSelectedColor(new Color(250, 50, 50));
        }else {
            gameVariables.setUserToken("");
            gameVariables.setUsername(UUID.randomUUID().toString().split("-")[0]);
            gameVariables.setOnlineMode(false);
            gameVariables.setSelectedColor(Color.MAGENTA);
        }
        new Game(gameVariables);
    }

    private void playMP3FromResources(String filename) {
        try {
            URL songUrl = getClass().getClassLoader().getResource(filename);
            if (songUrl == null) {
                System.out.println("MP3 file not found: " + filename);
                return;
            }
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
