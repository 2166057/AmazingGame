package net.wattpadpremium.server;

import lombok.Getter;

public abstract class AbstractGameServer {

    @Getter
    private final AbstractTCPServer tcpServer;

    protected AbstractGameServer(AbstractTCPServer tcpServer) {
        this.tcpServer = tcpServer;
    }

    public abstract void playerJoinEvent(ServerPlayer serverPlayer);

    public abstract void playerQuitEvent(ServerPlayer serverPlayer);
}
