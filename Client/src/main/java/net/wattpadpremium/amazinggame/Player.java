package net.wattpadpremium.amazinggame;

import lombok.Data;

import java.awt.*;

@Data
public class Player extends Profile{

    private int x = 0, y = 0;
    private int score = 0;

}
