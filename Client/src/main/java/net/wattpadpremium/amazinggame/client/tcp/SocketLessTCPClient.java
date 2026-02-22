package net.wattpadpremium.amazinggame.client.tcp;

import lombok.Getter;
import net.wattpadpremium.Packet;
import net.wattpadpremium.listeners.PacketListener;
import net.wattpadpremium.server.socketless.SocketLessClientHandler;
import net.wattpadpremium.server.socketless.SocketLessTCPServer;

import java.util.HashMap;

public class SocketLessTCPClient extends AbstractTCPClient {

    private SocketLessClientHandler socketLessClientHandler;

    private SocketLessTCPServer fakeServerSocket;

    @Getter
    private final HashMap<Integer, PacketListener> packetHandler = new HashMap<>();

    @Override
    public void sendPacketToServer(Packet packet) {
        System.out.println(this.getClass().getName() + " is sending Packet " +packet.getPacketId()+ " to Server");
        fakeServerSocket.receivePacket(packet, socketLessClientHandler);
    }

    public void receivePacket(Packet packet) {
        if (packet != null && packetHandler.containsKey(packet.getPacketId())) {
            PacketListener packetListener = packetHandler.get(packet.getPacketId());
            packetListener.handlePacket(packet);
        }
    }

    @Override
    public void stopClient() {

    }

    public SocketLessClientHandler requestSocketLessClientHandler(SocketLessTCPServer fakeServerSocket) {
        this.fakeServerSocket = fakeServerSocket;
        var socketLessClientHandler = new SocketLessClientHandler(fakeServerSocket);
        socketLessClientHandler.onSendPacketToClient((packet)->receivePacket(packet));
        fakeServerSocket.getClientHandlers().add(socketLessClientHandler);
        this.socketLessClientHandler = socketLessClientHandler;
        return socketLessClientHandler;
    }
}
