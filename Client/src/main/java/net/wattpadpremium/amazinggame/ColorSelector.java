package net.wattpadpremium.amazinggame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ColorSelector extends JDialog {
    private Color selectedColor;
    private final JColorChooser colorChooser;

    public ColorSelector(JFrame parent, Color initialColor) {
        super(parent, "Choose Color", true);
        selectedColor = initialColor;

        colorChooser = new JColorChooser(selectedColor);
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedColor = colorChooser.getColor();
                dispose();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        setLayout(new BorderLayout());
        add(colorChooser, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    public Color getSelectedColor() {
        return selectedColor;
    }
}
