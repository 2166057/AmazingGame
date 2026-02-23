package net.wattpadpremium.amazinggame.client;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

import static net.wattpadpremium.amazinggame.client.GameColors.*;

public class MultiplayerMenu {





    public MultiplayerMenu(Game game) {


    }




    // ─────────────────────────────────────────────────────────────────────────
    //  Main painted panel
    // ─────────────────────────────────────────────────────────────────────────

    public static class MultiplayerPanel extends JPanel {

        private final StyledTextField serverAddressField;
        private final StyledTextField portField;
        private final StyledButton    connectButton;
        private final StyledButton    backButton;
        private String errorMessage = "";

        public MultiplayerPanel(Game game) {
            serverAddressField = new StyledTextField("127.0.0.1");
            portField          = new StyledTextField("12345");
            connectButton      = new StyledButton("Connect");
            backButton         = new StyledButton("← Back");

            setLayout(null);
            setBackground(BG_DARK);
            add(serverAddressField);
            add(portField);
            add(connectButton);
            add(backButton);




            backButton.addActionListener(e -> {
                game.getScreen().setScreenState(Screen.ScreenState.MAINMENU);
            });

            connectButton.addActionListener(e -> {
                String serverAddress = serverAddressField.getText().trim();
                String portText      = portField.getText().trim();
                try {
                    int port = Integer.parseInt(portText);
                    if (port < 1024 || port > 65535) {
                        setError("Port must be between 1024 and 65535");
                    } else {
                        setError("");
                        connectButton.setEnabled(false);
                        game.joinServer(serverAddress, port);
                    }
                } catch (NumberFormatException ex) {
                    setError("Please enter a valid port number");
                }
            });
        }

        @Override
        public void doLayout() {
            super.doLayout();
            positionFields();
        }

        private void positionFields() {
            int w = getWidth(), h = getHeight();

            int formW   = Math.min(400, w / 3);
            int fieldH  = 42;
            int btnH    = 52;
            int gap     = 14;
            int labelH  = 20;
            int blockH  = labelH + gap/2 + fieldH   // server address
                    + gap
                    + labelH + gap/2 + fieldH   // port
                    + gap * 2
                    + btnH;
            int startX  = (w - formW) / 2;
            int startY  = h / 2 - blockH / 2 + 30;  // +30: sits just below title

            int y = startY;
            serverAddressField.setBounds(startX, y + labelH + gap / 2, formW, fieldH);
            y += labelH + gap / 2 + fieldH + gap;

            portField.setBounds(startX, y + labelH + gap / 2, formW, fieldH);
            y += labelH + gap / 2 + fieldH + gap * 2;

            connectButton.setBounds(startX, y, formW, btnH);

            // Back button — small, top-left corner
            backButton.setBounds(20, 20, 100, 36);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // Radial vignette (same as GameMenu)
            RadialGradientPaint vignette = new RadialGradientPaint(
                    w / 2f, h / 2f,
                    Math.max(w, h) * 0.65f,
                    new float[]{ 0f, 1f },
                    new Color[]{ BG_MID, BG_DARK }
            );
            g2.setPaint(vignette);
            g2.fillRect(0, 0, w, h);

            // ── Title ─────────────────────────────────────────────────────
            int ruleY = h / 2 - 160;

            g2.setColor(SEPARATOR);
            g2.drawLine(w / 4, ruleY, 3 * w / 4, ruleY);

            Font titleFont = g2.getFont().deriveFont(Font.BOLD, 36f);
            g2.setFont(titleFont);
            FontMetrics fm = g2.getFontMetrics();
            String title = "MULTIPLAYER";
            int tx = (w - fm.stringWidth(title)) / 2;
            int ty = ruleY - 16;

            g2.setColor(new Color(100, 180, 255, 30));
            g2.drawString(title, tx + 2, ty + 2);
            g2.setColor(TEXT_PRIMARY);
            g2.drawString(title, tx, ty);

            // Accent dots
            g2.setColor(ACCENT);
            int dotY = ruleY - 4, dotGap = 6;
            int dotCount = 5, dotTotalW = dotCount * 4 + (dotCount - 1) * dotGap;
            int dotStartX = (w - dotTotalW) / 2;
            for (int i = 0; i < dotCount; i++) {
                g2.fillOval(dotStartX + i * (4 + dotGap), dotY, 4, 4);
            }

            Font subFont = g2.getFont().deriveFont(Font.PLAIN, 13f);
            g2.setFont(subFont);
            fm = g2.getFontMetrics();
            String sub = "Enter server details to connect";
            g2.setColor(TEXT_DIM);
            g2.drawString(sub, (w - fm.stringWidth(sub)) / 2, ruleY + 20);

            // ── Field labels ──────────────────────────────────────────────
            int formW  = Math.min(400, w / 3);
            int startX = (w - formW) / 2;

            Font labelFont = g2.getFont().deriveFont(Font.BOLD, 11f);
            g2.setFont(labelFont);
            g2.setColor(TEXT_DIM);

            Rectangle sfBounds = serverAddressField.getBounds();
            Rectangle pfBounds = portField.getBounds();

            if (sfBounds.height > 0) {
                g2.drawString("SERVER ADDRESS", startX, sfBounds.y - 6);
                g2.drawString("PORT",           startX, pfBounds.y - 6);
            }

            // ── Error message ─────────────────────────────────────────────
            if (!errorMessage.isEmpty()) {
                Font errFont = g2.getFont().deriveFont(Font.PLAIN, 12f);
                g2.setFont(errFont);
                fm = g2.getFontMetrics();
                g2.setColor(ERROR_COLOR);
                Rectangle cbBounds = connectButton.getBounds();
                g2.drawString(errorMessage,
                        (w - fm.stringWidth(errorMessage)) / 2,
                        cbBounds.y + cbBounds.height + 22);
            }
        }

        private void setError(String msg) {
            errorMessage = msg;
            repaint();
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    //  Styled text field
    // ─────────────────────────────────────────────────────────────────────────

    private static class StyledTextField extends JTextField {

        StyledTextField(String defaultText) {
            super(defaultText);
            setOpaque(false);
            setForeground(TEXT_PRIMARY);
            setCaretColor(ACCENT);
            setFont(getFont().deriveFont(Font.PLAIN, 14f));
            setHorizontalAlignment(JTextField.LEFT);

            // Replace border with our own insets
            setBorder(new Border() {
                @Override public Insets getBorderInsets(Component c) { return new Insets(0, 12, 0, 12); }
                @Override public boolean isBorderOpaque()            { return false; }
                @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {}
            });

            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e)   { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            boolean focused = isFocusOwner();

            // Background
            g2.setColor(FIELD_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 8, 8));

            // Border — accent when focused
            g2.setColor(focused ? ACCENT : FIELD_BORDER);
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 8, 8));

            // Left accent bar when focused
            if (focused) {
                g2.setColor(ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, h * 0.2f, 3, h * 0.6f, 3, 3));
            }

            super.paintComponent(g);
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    //  Styled button (identical to GameMenu)
    // ─────────────────────────────────────────────────────────────────────────

    private static class StyledButton extends JComponent {

        private final String  label;
        private       boolean hovered = false;
        private       boolean pressed = false;
        private       boolean enabled = true;

        StyledButton(String label) {
            this.label = label;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e)  { if (enabled) { hovered = true;  repaint(); } }
                @Override public void mouseExited(MouseEvent e)   { hovered = false; pressed = false; repaint(); }
                @Override public void mousePressed(MouseEvent e)  { if (enabled) { pressed = true;  repaint(); } }
                @Override public void mouseReleased(MouseEvent e) {
                    if (enabled && hovered && pressed) fireActionPerformed();
                    pressed = false;
                    repaint();
                }
            });
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
            setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());
            repaint();
        }

        void addActionListener(ActionListener l) {
            listenerList.add(ActionListener.class, l);
        }

        private void fireActionPerformed() {
            for (ActionListener l : listenerList.getListeners(ActionListener.class)) {
                l.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, label));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            g2.setColor(!enabled ? new Color(35, 35, 42) : pressed ? ACCENT.darker() : hovered ? BTN_HOVER : BTN_NORMAL);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 10, 10));

            if (enabled && (hovered || pressed)) {
                g2.setColor(pressed ? ACCENT_BRIGHT : ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, h * 0.2f, 3, h * 0.6f, 3, 3));
            }

            g2.setColor(enabled && hovered ? ACCENT : BTN_BORDER);
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 10, 10));

            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(!enabled ? TEXT_DIM : pressed ? Color.WHITE : hovered ? ACCENT_BRIGHT : TEXT_PRIMARY);
            g2.drawString(label,
                    (w - fm.stringWidth(label)) / 2,
                    (h + fm.getAscent() - fm.getDescent()) / 2);
        }
    }
}