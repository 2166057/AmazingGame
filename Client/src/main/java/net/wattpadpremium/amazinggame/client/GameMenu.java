package net.wattpadpremium.amazinggame.client;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameMenu extends JFrame {

    private JPanel mainPanel;

    private final GameInstance gameInstance;

    public GameMenu(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
        setTitle("Game Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(3, 1));

        JButton multiPlayerButton = new JButton("Multiplayer");
        JButton colorButton = new JButton("Choose Player Color");
        JButton exitButton = new JButton("Exit");

        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showColorSelector();
            }
        });

        exitButton.addActionListener(e -> System.exit(0));

        multiPlayerButton.addActionListener(e -> new MultiplayerMenu(gameInstance,this).setVisible(true));

        mainPanel.add(multiPlayerButton);
        mainPanel.add(colorButton);
        mainPanel.add(exitButton);

        add(mainPanel);
    }

    private void showColorSelector() {
        ColorSelector colorSelector = new ColorSelector(this, gameInstance.getProfile().getColor());
        gameInstance.getProfile().setColor(colorSelector.getSelectedColor());
        mainPanel.setBackground(gameInstance.getProfile().getColor());
    }


}
