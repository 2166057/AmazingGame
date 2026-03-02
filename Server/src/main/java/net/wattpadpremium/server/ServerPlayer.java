package net.wattpadpremium.server;

import lombok.Getter;
import lombok.Setter;
import net.wattpadpremium.Packet;


public class ServerPlayer {

    private final AbstractGameServer gameServer;

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

    private final IClientHandler clientHandler;

    public ServerPlayer(AbstractGameServer abstractGameServer, IClientHandler clientHandler, Long id, String username, int color) {
        this.gameServer = abstractGameServer;
        this.clientHandler = clientHandler;
        this.username = username;
        this.color = color;
        this.playerId = id;
    }

    public void setStatus(PlayerStatusPacket.STATUS status, boolean enabled){
        PlayerStatusPacket playerStatusPacket = new PlayerStatusPacket(new PlayerStatusPacket.Data(getPlayerId(), status, enabled));
        sendPacketToClient(playerStatusPacket);
    }

    public void setScore(int score){
        this.score = score;
        PlayerScorePacket playerScorePacket = new PlayerScorePacket(new PlayerScorePacket.Data(getPlayerId(), score));
        gameServer.getTcpServer().broadcastPacket(playerScorePacket);
    }

    public void sendPacketToClient(Packet<?> packet){
        clientHandler.sendPacketToClient(packet);
    }

    public void onConnectMatch(){
        gameServer.playerJoinEvent(this);
    }

    public void onDisconnect(){
        gameServer.playerQuitEvent(this);
    }
}
