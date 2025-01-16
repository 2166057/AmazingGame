package net.wattpadpremium.listeners;

import net.wattpadpremium.Packet;

public interface PacketListener {

    void handlePacket(Packet packet);

}
