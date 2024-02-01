package net.wattpadpremium.amazinggame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimerTask;
import java.util.Timer;

public class GameMenu extends JFrame {
    private JPanel mainPanel;

    public GameMenu() {
        setTitle("Game Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(3, 1));

        JButton startButton = new JButton("Start Game");
        JButton colorButton = new JButton("Choose Player Color");
        JButton exitButton = new JButton("Exit");

        startButton.addActionListener(e -> {
            MazeGame mazeGame = new MazeGame();
            mazeGame.setVisible(true);

            java.util.Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mazeGame.handleContinuousMovement();
                    mazeGame.repaint();
                }
            }, 0, 50);
        });

        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showColorSelector();
            }
        });

        exitButton.addActionListener(e -> System.exit(0));

        mainPanel.add(startButton);
        mainPanel.add(colorButton);
        mainPanel.add(exitButton);

        add(mainPanel);
    }

    private void showColorSelector() {
        ColorSelector colorSelector = new ColorSelector(this, GameStart.selectedColor);
        GameStart.selectedColor = colorSelector.getSelectedColor();
        mainPanel.setBackground(GameStart.selectedColor);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameMenu().setVisible(true));
    }
}
