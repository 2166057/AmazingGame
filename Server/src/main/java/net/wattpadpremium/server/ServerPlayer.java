package net.wattpadpremium.server;

import lombok.Getter;
import lombok.Setter;
import net.wattpadpremium.Packet;
import net.wattpadpremium.PlayerScorePacket;
import net.wattpadpremium.PlayerStatusPacket;

import java.awt.*;

public class ServerPlayer {

    @Getter
    private final String username;
    @Getter
    private final int color;
    @Getter
    private int score = 0;

    @Getter
    @Setter
    private int x = 0, y = 0;

    private final TCPServer.ClientHandler clientHandler;
    private final GameServer gameServer;

    public ServerPlayer(GameServer gameServer, TCPServer.ClientHandler clientHandler, String username, int color) {
        this.clientHandler = clientHandler;
        this.gameServer = gameServer;
        this.username = username;
        this.color = color;
        clientHandler.setServerPlayer(this);
        gameServer.playerJoinEvent(this);
    }

    public void setStatus(PlayerStatusPacket.STATUS status, boolean enabled){
        PlayerStatusPacket playerStatusPacket = new PlayerStatusPacket(getUsername(), status, enabled);
        sendPacket(playerStatusPacket);
    }

    public void setBlind(boolean blind){
        setStatus(PlayerStatusPacket.STATUS.BLINDED, blind);
    }

    public void setDizzy(boolean dizzy){
        setStatus(PlayerStatusPacket.STATUS.DIZZY, dizzy);
    }

    public void setScore(int score){
        this.score = score;
        PlayerScorePacket playerScorePacket = new PlayerScorePacket(username, score);
        gameServer.tcpServer.broadcastPacket(playerScorePacket);
    }

    public void sendPacket(Packet packet){
        clientHandler.sendPacket(packet);
    }

    public void onDisconnect(){
        gameServer.playerQuitEvent(this);
    }
}
