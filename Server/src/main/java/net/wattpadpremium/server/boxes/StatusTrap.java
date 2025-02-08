package net.wattpadpremium.server.boxes;

import lombok.Getter;
import net.wattpadpremium.server.PlayerStatusPacket;
import net.wattpadpremium.server.GameServerAPI;
import net.wattpadpremium.server.ServerPlayer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public class StatusTrap extends Trap{

    private final PlayerStatusPacket.STATUS status;

    public StatusTrap(GameServerAPI gameServer, int posX, int posY, PlayerStatusPacket.STATUS status) {
        super(gameServer, posX, posY);
        this.status = status;
    }

    @Override
    public void onTrigger(GameServerAPI gameServer, ServerPlayer serverPlayer) {
        if (this.isVisible()){
            serverPlayer.setStatus(status, true);
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.schedule(() -> {
                serverPlayer.setStatus(status, false);
                scheduler.shutdown();
            }, 5, TimeUnit.SECONDS);
            this.setDeleted(true);
        }else {
            this.changeVisibility(true);
        }
    }
}
