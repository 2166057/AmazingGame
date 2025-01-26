package net.wattpadpremium.amazinggame.client;

import java.awt.*;
import java.util.UUID;

public class ClientObject {

    final int x,y;
    final Color color;
    final UUID objectUUID;

    public ClientObject(int x, int y, Color color, UUID objectUUID) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.objectUUID = objectUUID;
    }

}
