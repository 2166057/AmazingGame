package net.wattpadpremium.server.socketless;

import lombok.Getter;
import lombok.Setter;
import net.wattpadpremium.Packet;
import net.wattpadpremium.server.IClientHandler;
import net.wattpadpremium.server.ServerPlayer;

import java.util.function.Consumer;

public class SocketLessClientHandler implements IClientHandler {

    public SocketLessClientHandler(SocketLessTCPServer socketLessTCPServer) {
        this.socketLessTCPServer = socketLessTCPServer;
    }

    @Getter
    @Setter
    private ServerPlayer serverPlayer;

    private final SocketLessTCPServer socketLessTCPServer;

    private Consumer<Packet<?>> receivePacketConsumer;

    @Override
    public void sendPacketToClient(Packet<?> packet) {
        System.out.println(this.getClass().getName()+ " is sending Packet "+ packet.getPacketType() +" to this client" + serverPlayer.getPlayerId());
        receivePacketConsumer.accept(packet);
    }

    public void onSendPacketToClient(Consumer<Packet<?>> packetConsumer) {
        this.receivePacketConsumer = packetConsumer;
    }
}
