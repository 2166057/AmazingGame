package net.wattpadpremium.server;

import lombok.Getter;
import lombok.Setter;
import net.wattpadpremium.Packet;

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

    @Getter
    private final Long playerId;

    private final TCPServer.ClientHandler clientHandler;
    private final GameServer gameServer;

    public ServerPlayer(GameServer gameServer, TCPServer.ClientHandler clientHandler, Long id, String username, int color) {
        this.clientHandler = clientHandler;
        this.gameServer = gameServer;
        this.username = username;
        this.color = color;
        this.playerId = id;
        clientHandler.setServerPlayer(this);
    }

    public void onConnectMatch(){
        gameServer.playerJoinEvent(this);
    }

    public void setStatus(PlayerStatusPacket.STATUS status, boolean enabled){
        PlayerStatusPacket playerStatusPacket = new PlayerStatusPacket(getPlayerId(), status, enabled);
        sendPacket(playerStatusPacket);
    }

    public void setScore(int score){
        this.score = score;
        PlayerScorePacket playerScorePacket = new PlayerScorePacket(getPlayerId(), score);
        gameServer.tcpServer.broadcastPacket(playerScorePacket);
    }

    public void sendPacket(Packet packet){
        clientHandler.sendPacket(packet);
    }

    public void onDisconnect(){
        gameServer.playerQuitEvent(this);
    }
}
