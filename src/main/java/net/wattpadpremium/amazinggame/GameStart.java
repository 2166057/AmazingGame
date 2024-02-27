package net.wattpadpremium.amazinggame;

import javax.swing.*;
import java.awt.*;

public class GameStart {
    public static Color selectedColor = new Color(231, 38, 116);
    public static int score = 0;
    public static Integer timeLeft;
    public static int best_score = 0;
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameMenu().setVisible(true);
            /* MazeGame mazeGame = new MazeGame();
            mazeGame.setVisible(true);

            java.util.Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mazeGame.handleContinuousMovement();
                    mazeGame.repaint();
                }
            }, 0, 50); // Adjust the interval as needed
            */
        });
    }


}
