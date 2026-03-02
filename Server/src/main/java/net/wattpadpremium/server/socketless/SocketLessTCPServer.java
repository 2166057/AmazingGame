package net.wattpadpremium.server.socketless;

import lombok.Getter;
import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketType;
import net.wattpadpremium.server.AbstractTCPServer;
import net.wattpadpremium.server.handler.ServerPacketListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class SocketLessTCPServer extends AbstractTCPServer {

    @Getter
    private final List<SocketLessClientHandler> clientHandlers = new ArrayList<>();

    @Getter
    private final HashMap<PacketType, ServerPacketListener> serverPacketHandler = new HashMap<>();

    private Consumer<Packet<?>> receievePacketCallback;

    @Override
    public void startServer() {

    }

    public void setSocketLessTCPClientReceivePacketCallback(Consumer<Packet<?>> callback) {
        this.receievePacketCallback = callback;
    }


}
