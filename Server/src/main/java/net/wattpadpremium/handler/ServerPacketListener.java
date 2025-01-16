package net.wattpadpremium.handler;

import net.wattpadpremium.Packet;
import net.wattpadpremium.TCPServer;

public interface ServerPacketListener {

    void handlePacket(Packet packet, TCPServer.ClientHandler clientHandler);

}
