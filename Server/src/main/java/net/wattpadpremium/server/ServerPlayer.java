package net.wattpadpremium.server;

import lombok.Data;
import net.wattpadpremium.Packet;

@Data
public class ServerPlayer {

    private String username;
    private int color;
    private int score = 0;
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

    public void sendPacket(Packet packet){
        clientHandler.sendPacket(packet);
    }

    public void onDisconnect(){
        gameServer.playerQuitEvent(this);
    }
}
