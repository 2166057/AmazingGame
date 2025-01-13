package net.wattpadpremium.amazinggame;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameLauncher extends JFrame {

    private final Profile profile = new Profile();


    public GameLauncher() {
        setTitle("Game Launcher");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 2, 5, 5));

        JLabel usernameLabel = new JLabel("Enter Username:");
        JTextField usernameField = new JTextField();
        usernameField.setText(profile.getUsername());

        inputPanel.add(usernameLabel);
        inputPanel.add(usernameField);

        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton launchButton = new JButton("Launch Game");

        buttonPanel.add(launchButton);
        add(buttonPanel, BorderLayout.SOUTH);

        launchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(GameLauncher.this, "Username cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    profile.setUsername(username);
                    new GameInstance(profile);
                    dispose(); // Close the launcher window
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameLauncher launcher = new GameLauncher();
            launcher.setVisible(true);
        });
        SwingUtilities.invokeLater(() -> {
            GameLauncher launcher = new GameLauncher();
            launcher.setVisible(true);
        });
    }
}


