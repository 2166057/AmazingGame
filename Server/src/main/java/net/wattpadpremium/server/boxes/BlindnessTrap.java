package net.wattpadpremium.server.boxes;

import net.wattpadpremium.PlayerStatusPacket;
import net.wattpadpremium.server.GameServerAPI;

public class BlindnessTrap extends StatusTrap{

    public BlindnessTrap(GameServerAPI gameServer, int posX, int posY) {
        super(gameServer, posX, posY, PlayerStatusPacket.STATUS.BLINDED);
    }

}
