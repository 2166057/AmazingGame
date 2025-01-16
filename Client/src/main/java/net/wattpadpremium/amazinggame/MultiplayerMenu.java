package net.wattpadpremium.amazinggame;

import net.wattpadpremium.*;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class MultiplayerMenu extends JFrame {

    private TCPClient tcpClient;

    private final JTextField serverAddressField;
    private final JTextField portField;

    private final JLabel playerLabel = new JLabel("");

    public MultiplayerMenu(GameInstance gameInstance,GameMenu gameMenu) {
        gameMenu.setVisible(false);
        setTitle("Multiplayer Menu");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        JLabel serverLabel = new JLabel("Server Address:");
        serverAddressField = new JTextField(20);
        serverAddressField.setText("127.0.0.1");


        JLabel portLabel = new JLabel("Port:");
        portField = new JTextField(20);
        portField.setText("12345");

        JButton connectButton = new JButton("Connect");

        // Add action listener for the button
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String serverAddress = serverAddressField.getText();
                String portText = portField.getText();
                
                // Validate the input
                try {
                    int port = Integer.parseInt(portText);
                    if (port < 1024 || port > 65535) {
                        JOptionPane.showMessageDialog(MultiplayerMenu.this, 
                            "Port must be between 1024 and 65535", 
                            "Invalid Port", 
                            JOptionPane.ERROR_MESSAGE);
                    } else {
                        connectButton.setEnabled(false);

                        tcpClient = new TCPClient(serverAddress, port);
                        tcpClient.getPacketHandler().put(MazePacket.ID, (packet)->{
                            MazePacket mazePacket = (MazePacket) packet;
                            if (gameInstance.getMazeGame() == null){
                                gameInstance.setMazeGame(new GameScene(tcpClient, gameInstance, mazePacket));
                                setVisible(false);
                            }else {
                                gameInstance.getMazeGame().updateMaze(mazePacket);
                            }
                        });
                        tcpClient.getPacketHandler().put(PositionChangePacket.ID, (packet) -> {
                            PositionChangePacket positionChangePacket = (PositionChangePacket) packet;
                            if (positionChangePacket.getUsername().equalsIgnoreCase(gameInstance.getProfile().getUsername().toLowerCase())){
                                gameInstance.getMazeGame().setLocalePosition(positionChangePacket.getX(), positionChangePacket.getY());
                            }else {
                                gameInstance.getMazeGame().setSpecificPlayerPos(positionChangePacket.getUsername(), positionChangePacket.getX(), positionChangePacket.getY());
                                gameInstance.getMazeGame().changeSpecificPlayerColor(positionChangePacket.getUsername(), positionChangePacket.getColor());
                            }
                        });
                        tcpClient.getPacketHandler().put(PlayerScorePacket.ID, (packet) -> {
                            PlayerScorePacket playerScorePacket = (PlayerScorePacket) packet;
                            if (playerScorePacket.getUsername().equalsIgnoreCase(gameInstance.getProfile().getUsername().toLowerCase())){
                                gameInstance.getMazeGame().setMyScore(playerScorePacket.getScore());
                            }else {
                                gameInstance.getMazeGame().changeSpecificPlayerScore(playerScorePacket.getUsername(), playerScorePacket.getScore());
                            }
                        });
                        tcpClient.getPacketHandler().put(EndGamePacket.ID, (packet) -> {
                            EndGamePacket endGamePacket = (EndGamePacket) packet;
                            if (gameInstance.getMazeGame() != null){
                                gameInstance.getMazeGame().endGame();
                            }
                            tcpClient.stopClient();
                            gameInstance.setMazeGame(null);
                            connectButton.setEnabled(true);
                            setVisible(true);
                        });
                        tcpClient.getPacketHandler().put(PlayerCountPacket.ID, packet -> {
                            PlayerCountPacket playerCountPacket = (PlayerCountPacket) packet;
                            playerLabel.setText("Waiting for players "+playerCountPacket.getCount() + "/" + playerCountPacket.getMax());
                        });
                        JoinPacket joinRequestPacket = new JoinPacket();
                        joinRequestPacket.setUsername(gameInstance.getProfile().getUsername());
                        joinRequestPacket.setColor(gameInstance.getProfile().getColor().getRGB());
                        tcpClient.sendPacket(joinRequestPacket);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(MultiplayerMenu.this, 
                        "Please enter a valid port number.", 
                        "Invalid Port", 
                        JOptionPane.ERROR_MESSAGE);
                } catch (IOException ex) {
                    connectButton.setEnabled(true);
                    throw new RuntimeException(ex);
                }
            }
        });

        // Add the components to the frame
        add(serverLabel);
        add(serverAddressField);
        add(portLabel);
        add(portField);
        add(connectButton);
        add(playerLabel);
    }

}