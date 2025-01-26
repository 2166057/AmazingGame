package net.wattpadpremium.server.boxes;

import net.wattpadpremium.server.GameServer;
import net.wattpadpremium.server.GameServerAPI;
import net.wattpadpremium.server.ServerPlayer;

public class RestartTrap extends Trap{

    public RestartTrap(GameServer gameServer, int posX, int posY) {
        super(gameServer, posX, posY);
    }

    @Override
    public void onTrigger(GameServerAPI gameServer, ServerPlayer serverPlayer) {
        if (isVisible()){
            serverPlayer.setX(gameServer.getSpawnX());
            serverPlayer.setY(gameServer.getSpawnY());
            gameServer.notifyPositionChangeToClients(serverPlayer);
            setDeleted(true);
        }else {
            this.changeVisibility(true);
        }
    }
}
