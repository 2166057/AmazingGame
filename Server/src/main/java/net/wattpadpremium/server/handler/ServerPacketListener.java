package net.wattpadpremium.server.handler;

import net.wattpadpremium.Packet;
import net.wattpadpremium.server.TCPServer;

public interface ServerPacketListener {

    void handlePacket(Packet packet, TCPServer.ClientHandler clientHandler);

}
