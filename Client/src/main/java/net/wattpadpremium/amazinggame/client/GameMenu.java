package net.wattpadpremium.amazinggame.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameMenu extends JFrame {

    private JPanel mainPanel;

    private final Game gameInstance;

    public GameMenu(Game gameInstance) {
        this.gameInstance = gameInstance;
        setTitle("Game Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(2, 2));

        JButton singlePlayerButton = new JButton("Single Player");
        JButton multiPlayerButton = new JButton("Multiplayer");
        if (!gameInstance.getGameVariables().getOnlineMode()){
            multiPlayerButton.setEnabled(false);
        }
        JButton colorButton = new JButton("Choose Player Color");
        JButton exitButton = new JButton("Exit");

        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showColorSelector();
            }
        });

        exitButton.addActionListener(e -> System.exit(0));

        multiPlayerButton.addActionListener(e -> {
            gameInstance.getMultiplayerMenu().setVisible(true);
            gameInstance.getMainMenu().setVisible(false);
        });

        singlePlayerButton.addActionListener(e->{
            gameInstance.getMainMenu().setVisible(false);
            gameInstance.getPlayScene().joinSinglePlayer();
        });

        mainPanel.add(singlePlayerButton);
        mainPanel.add(multiPlayerButton);
        mainPanel.add(colorButton);
        mainPanel.add(exitButton);

        add(mainPanel);
    }

    private void showColorSelector() {
        ColorSelector colorSelector = new ColorSelector(this, gameInstance.getGameVariables().getSelectedColor());
        gameInstance.getGameVariables().setSelectedColor(colorSelector.getSelectedColor());
        mainPanel.setBackground(gameInstance.getGameVariables().getSelectedColor());
    }


}
