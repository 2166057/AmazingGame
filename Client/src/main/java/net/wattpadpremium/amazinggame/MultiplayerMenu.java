package net.wattpadpremium.amazinggame;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class MultiplayerMenu extends JFrame {
    
    private JTextField serverAddressField;
    private JTextField portField;
    private JButton connectButton;

    private final GameMenu gameMenu;

    public MultiplayerMenu(GameMenu gameMenu) {
        this.gameMenu = gameMenu;
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

        JLabel portLabel = new JLabel("Port:");
        portField = new JTextField(20);  // 20 columns wide

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
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(MultiplayerMenu.this, 
                        "Please enter a valid port number.", 
                        "Invalid Port", 
                        JOptionPane.ERROR_MESSAGE);
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