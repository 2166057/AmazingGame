package net.wattpadpremium.server.modes;

import lombok.Getter;

@Getter
public enum GameMode {

    TIMER(1,1), INFINITE(1, 1), SCORELIMIT(1,100);
//    ELIMINATION(3,100);

    private final int minPlayer;
    private final int maxPlayer;

    GameMode(int minPlayer, int maxPlayer) {
        this.minPlayer = minPlayer;
        this.maxPlayer = maxPlayer;
    }

}
