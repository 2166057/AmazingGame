package net.wattpadpremium.server;

import net.wattpadpremium.Packet;

public interface IClientHandler {

    void sendPacketToClient(Packet<?> packet);
    void setServerPlayer(ServerPlayer serverPlayer);
    ServerPlayer getServerPlayer();

}
