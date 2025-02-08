package net.wattpadpremium.amazinggame.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MultiplayerMenu extends JFrame {

    private final JTextField serverAddressField;
    private final JTextField portField;

    private final JLabel playerLabel = new JLabel("");

    public MultiplayerMenu(Game game) {
        setTitle("Multiplayer Menu");
        setVisible(false);
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

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String serverAddress = serverAddressField.getText();
                String portText = portField.getText();
                
                try {
                    int port = Integer.parseInt(portText);
                    if (port < 1024 || port > 65535) {
                        JOptionPane.showMessageDialog(MultiplayerMenu.this, 
                            "Port must be between 1024 and 65535", 
                            "Invalid Port", 
                            JOptionPane.ERROR_MESSAGE);
                    } else {
                        connectButton.setEnabled(false);
                        game.getPlayScene().joinServer(serverAddress, port);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(MultiplayerMenu.this, 
                        "Please enter a valid port number.", 
                        "Invalid Port", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        add(serverLabel);
        add(serverAddressField);
        add(portLabel);
        add(portField);
        add(connectButton);
        add(playerLabel);
    }

}