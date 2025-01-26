package net.wattpadpremium.server.boxes;

import lombok.Getter;
import lombok.Setter;
import net.wattpadpremium.server.GameServerAPI;
import net.wattpadpremium.server.ServerPlayer;

import java.util.UUID;

public abstract class Trap {

    private final GameServerAPI gameServer;

    public Trap(GameServerAPI gameServer, int posX, int posY){
        this.gameServer = gameServer;
        this.trapUUID = UUID.randomUUID();
        this.posX = posX;
        this.posY = posY;
    }

    @Getter
    private final UUID trapUUID;
    @Getter
    private boolean visible = false;

    @Getter
    @Setter
    private boolean deleted = false;

    @Getter
    private final int posX, posY;

    public abstract void onTrigger(GameServerAPI gameServer, ServerPlayer serverPlayer);

    public void changeVisibility(boolean visible) {
        if (this.visible != visible){
            this.visible = visible;
            gameServer.onVisibilityChange(this);
        }
    }

}
