package net.wattpadpremium.amazinggame.client;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;

@Data
public class Player {

    private Long playerId;
    private String username = "";
    private int x = 0, y = 0;
    private int score = 0;
    private Color color = new Color(231, 38, 116);

}
