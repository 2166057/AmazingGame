package net.wattpadpremium.amazinggame.client.tcp;

import lombok.Getter;
import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketType;
import net.wattpadpremium.listeners.PacketListener;
import net.wattpadpremium.server.socketless.SocketLessClientHandler;
import net.wattpadpremium.server.socketless.SocketLessTCPServer;

import java.util.HashMap;

public class SocketLessTCPClient extends AbstractTCPClient {

    private SocketLessClientHandler socketLessClientHandler;

    private SocketLessTCPServer fakeServerSocket;

    @Getter
    private final HashMap<PacketType, PacketListener> packetHandler = new HashMap<>();

    @Override
    public void sendPacketToServer(Packet<?> packet) {
        System.out.println(this.getClass().getName() + " is sending Packet " + packet.getPacketType()+ " to Server");
        fakeServerSocket.receivePacket(packet, socketLessClientHandler);
    }

    public void receivePacket(Packet<?> packet) {
        if (packet != null && packetHandler.containsKey(packet.getPacketType())) {
            PacketListener packetListener = packetHandler.get(packet.getPacketType());
            packetListener.handlePacket(packet);
        }
    }

    @Override
    public void stopClient() {

    }

    public SocketLessClientHandler requestSocketLessClientHandler(SocketLessTCPServer fakeServerSocket) {
        this.fakeServerSocket = fakeServerSocket;
        var socketLessClientHandler = new SocketLessClientHandler(fakeServerSocket);
        socketLessClientHandler.onSendPacketToClient(this::receivePacket);
        fakeServerSocket.getClientHandlers().add(socketLessClientHandler);
        this.socketLessClientHandler = socketLessClientHandler;
        return socketLessClientHandler;
    }
}
