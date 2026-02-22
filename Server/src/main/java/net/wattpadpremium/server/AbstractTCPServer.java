package net.wattpadpremium.server;

import net.wattpadpremium.Packet;
import net.wattpadpremium.server.handler.ServerPacketListener;

import java.util.HashMap;
import java.util.List;

public abstract class AbstractTCPServer {

    public abstract HashMap<Integer, ServerPacketListener> getServerPacketHandler();

    public abstract void startServer();

    public void receivePacket(Packet packet, IClientHandler clientHandler) {
        ServerPacketListener serverPacketListener = getServerPacketHandler().get(packet.getPacketId());
        if (serverPacketListener != null) {
            serverPacketListener.handlePacket(packet, clientHandler);
        }else {
            System.out.println(packet.getPacketId()+ " packet not found!");
        }
    }

    public abstract List<? extends IClientHandler> getClientHandlers();

    public void broadcastPacket(Packet packet) {
        final var clientHandlers = getClientHandlers();
        synchronized (clientHandlers){
            for (IClientHandler clientHandler : clientHandlers) {
                clientHandler.sendPacketToClient(packet);
            }
        }
    }

}
