package net.wattpadpremium.server;

import net.wattpadpremium.server.boxes.Trap;

import java.util.List;

public interface GameServerAPI {

    int[][] getMaze();

    int getGoalX();

    int getGoalY();

    ServerPlayer getServerPlayer(String username);

    List<ServerPlayer> getServerPlayers();

    int getSpawnX();

    int getSpawnY();

    void notifyPositionChangeToClients(ServerPlayer serverPlayer);

    void onVisibilityChange(Trap trap);

    void removeTrap(Trap trap);

    void spawnTrap(Trap trap);
}
