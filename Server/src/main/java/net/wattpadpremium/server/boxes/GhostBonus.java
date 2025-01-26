package net.wattpadpremium.server.boxes;

import net.wattpadpremium.PlayerStatusPacket;
import net.wattpadpremium.server.GameServerAPI;

public class GhostBonus extends StatusTrap{

    public GhostBonus(GameServerAPI gameServer, int posX, int posY) {
        super(gameServer, posX, posY, PlayerStatusPacket.STATUS.GHOSTING);
    }

}
