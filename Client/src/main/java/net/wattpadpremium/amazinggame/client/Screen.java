package net.wattpadpremium.amazinggame.client;

import lombok.Getter;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;



@Getter
public class Screen extends JFrame {

    private final Game instance;

    public enum  ScreenState {
        MAINMENU,
        MULTIPLAYERMENU,
        GAMEPLAY
    }

    private final PlayScene playScene;
    private final GameMenu.GameMenuPanel gameMenuPanel;
    private final MultiplayerMenu.MultiplayerPanel multiplayerMenu;



    @Getter
    private ScreenState screenState;

    public void setScreenState(ScreenState screenState) {
        this.screenState = screenState;

        JPanel newPanel = null;

        switch (screenState) {
            case MAINMENU -> newPanel = gameMenuPanel;
            case MULTIPLAYERMENU -> newPanel = multiplayerMenu;
            case GAMEPLAY -> {
                newPanel = playScene;
                playScene.setFocusTraversalKeysEnabled(false); // keep TAB for game
            }
        }

        if (newPanel != null) {
            this.setContentPane(newPanel);          // swap panel
            newPanel.setFocusable(true);            // allow key events
            SwingUtilities.invokeLater(newPanel::requestFocusInWindow); // ensure focus after layout
            this.getContentPane().revalidate();     // re-layout
            this.getContentPane().repaint();        // redraw
        }
    }

    public Screen(Game instance) {
        this.instance = instance;
        this.gameMenuPanel = new GameMenu.GameMenuPanel(instance);
        this.multiplayerMenu = new MultiplayerMenu.MultiplayerPanel(instance);
        this.playScene = new PlayScene(instance);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                instance.getTcpClient().stopClient();
            }
        });
    }

}
