package net.wattpadpremium.amazinggame.client;

import lombok.Getter;
import net.wattpadpremium.SessionManager;
import net.wattpadpremium.amazinggame.client.tcp.AbstractTCPClient;
import net.wattpadpremium.amazinggame.client.tcp.SocketLessTCPClient;
import net.wattpadpremium.amazinggame.client.tcp.TCPClient;
import net.wattpadpremium.client.AuthSessionPacket;
import net.wattpadpremium.server.GameServer;
import net.wattpadpremium.server.modes.GameMode;
import net.wattpadpremium.server.socketless.SocketLessClientHandler;
import net.wattpadpremium.server.socketless.SocketLessTCPServer;


import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Getter
public class Game {

    private final GameVariables gameVariables;

    private final Screen screen;

    private AbstractTCPClient tcpClient;


    public Game(GameVariables gameVariables) {
        this.gameVariables = gameVariables;
        this.screen = new Screen(this);
        new ResizableDragListener(screen);
        this.screen.setUndecorated(true);
        this.screen.setScreenState(Screen.ScreenState.MAINMENU);
        this.screen.setVisible(true);

//        playMP3FromResources("game_song.wav");
    }

    public static void main(String[] args) {
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


    // ─────────────────────────────────────────────────────────────────────────
    //  Connection helpers
    // ─────────────────────────────────────────────────────────────────────────

    public void joinSinglePlayer() {
        try {
            var fakeServerSocket = new SocketLessTCPServer();
            var server           = new GameServer(GameMode.TIMER, false, fakeServerSocket);
            var client           = new SocketLessTCPClient();
            SocketLessClientHandler handler = client.requestSocketLessClientHandler(fakeServerSocket);
            this.tcpClient = client;

            getScreen().getPlayScene().configureClientPacketListener(this.tcpClient);

            String username;
            String sessionToken;

            if (getGameVariables().getOnlineMode()) {
                username = getGameVariables().getUsername();
                sessionToken = SessionManager.createUserSessionToken(getGameVariables().getUserToken(), "-");
            } else {
                username = getGameVariables().getUsername();
                sessionToken = UUID.randomUUID().toString();
            }
            getScreen().getPlayScene().startTicking();
            tcpClient.sendPacketToServer(new AuthSessionPacket(new AuthSessionPacket.Data(username, sessionToken)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void joinServer(String address, int port) {
        try {
            this.tcpClient = new TCPClient(address, port);
            getScreen().getPlayScene().configureClientPacketListener(this.tcpClient);

            String username;
            String sessionToken;

            if (getGameVariables().getOnlineMode()) {
                username = getGameVariables().getUsername();
                sessionToken = SessionManager.createUserSessionToken(getGameVariables().getUserToken(), "-");
            } else {
                username = getGameVariables().getUsername();
                sessionToken = UUID.randomUUID().toString();
            }

            getScreen().getPlayScene().startTicking();
            tcpClient.sendPacketToServer(new AuthSessionPacket(new AuthSessionPacket.Data(username, sessionToken)));
        } catch (IOException | InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }
}
