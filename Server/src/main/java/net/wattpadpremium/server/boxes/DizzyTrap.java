package net.wattpadpremium.server.boxes;

import net.wattpadpremium.server.PlayerStatusPacket;
import net.wattpadpremium.server.GameServer;

public class DizzyTrap extends StatusTrap{

    public DizzyTrap(GameServer gameServer, int posX, int posY) {
        super(gameServer, posX, posY, PlayerStatusPacket.STATUS.DIZZY);
    }

}
