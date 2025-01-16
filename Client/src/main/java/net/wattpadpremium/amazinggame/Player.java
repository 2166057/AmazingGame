package net.wattpadpremium.amazinggame;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class Player extends Profile{

    private int x = 0, y = 0;
    private int score = 0;

}
