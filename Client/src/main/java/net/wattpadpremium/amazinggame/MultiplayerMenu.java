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

    private JTextField serverAddressField;
    private JTextField portField;
    private JButton connectButton;

    public MultiplayerMenu(GameInstance gameInstance,GameMenu gameMenu) {
        gameMenu.setVisible(false);
        // Set up the frame
        setTitle("Multiplayer Menu");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // Center the frame on the screen
        setLayout(new FlowLayout());

        // Create and add components
        JLabel serverLabel = new JLabel("Server Address:");
        serverAddressField = new JTextField(20);  // 20 columns wide
        serverAddressField.setText("127.0.0.1");


        JLabel portLabel = new JLabel("Port:");
        portField = new JTextField(20);  // 20 columns wide
        portField.setText("12345");

        connectButton = new JButton("Connect");

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
                        // Here you can use the serverAddress and port to connect to the server
                        JOptionPane.showMessageDialog(MultiplayerMenu.this, 
                            "Connecting to server: " + serverAddress + " on port " + port, 
                            "Connection Info", 
                            JOptionPane.INFORMATION_MESSAGE);


                        tcpClient = new TCPClient(serverAddress, port);
                        tcpClient.getPacketHandler().put(KeepAlivePacket.ID, packet->{
                            System.out.println("Received Packet");
                        });
                        tcpClient.getPacketHandler().put(MazePacket.ID, packet->{
                            MazePacket mazePacket = (MazePacket) packet;
                            if (gameInstance.getMazeGame() == null){
                                gameInstance.setMazeGame(new MazeGame(tcpClient, gameInstance, mazePacket));
                            }else {
                                gameInstance.getMazeGame().updateMaze(mazePacket);
                            }
                        });

                        tcpClient.getPacketHandler().put(PositionChangePacket.ID, packet -> {
                            PositionChangePacket positionChangePacket = (PositionChangePacket) packet;
                            if (positionChangePacket.getUsername().equalsIgnoreCase(gameInstance.getProfile().getUsername().toLowerCase())){
                                gameInstance.getMazeGame().setLocalePosition(positionChangePacket.getX(), positionChangePacket.getY());
                            }else {
                                gameInstance.getMazeGame().setSpecificPlayerPos(positionChangePacket.getUsername(), positionChangePacket.getX(), positionChangePacket.getY());
                                gameInstance.getMazeGame().changeSpecificPlayerColor(positionChangePacket.getUsername(), positionChangePacket.getColor());
                            }
                        });
                        tcpClient.getPacketHandler().put(PlayerScorePacket.ID, packet -> {
                            PlayerScorePacket playerScorePacket = (PlayerScorePacket) packet;
                            if (playerScorePacket.getUsername().equalsIgnoreCase(gameInstance.getProfile().getUsername().toLowerCase())){
                                gameInstance.getMazeGame().setMyScore(playerScorePacket.getScore());
                            }else {
                                gameInstance.getMazeGame().changeSpecificPlayerScore(playerScorePacket.getUsername(), playerScorePacket.getScore());
                            }
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
    }

}