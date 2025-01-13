package net.wattpadpremium;

import lombok.Data;

@Data
public class ServerPlayer {

    private String username;
    private int color;
    private int score = 0;
    private int x = 0, y = 0;


    public ServerPlayer(String username, int color) {
        this.username = username;
        this.color = color;
    }
}
