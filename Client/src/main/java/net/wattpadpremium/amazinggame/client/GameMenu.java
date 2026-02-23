package net.wattpadpremium.amazinggame.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

import static net.wattpadpremium.amazinggame.client.GameColors.*;

public class GameMenu {

    // ─────────────────────────────────────────────────────────────────────────
    //  Main painted panel
    // ─────────────────────────────────────────────────────────────────────────

    public static class GameMenuPanel extends JPanel {

        private final Game instance;

        public GameMenuPanel(Game instance) {
            this.instance = instance;
            setLayout(null); // absolute layout — we position buttons manually
            setBackground(BG_DARK);
            buildButtons();
        }

        // ── Build styled buttons ──────────────────────────────────────────

        private void buildButtons() {
            boolean onlineMode = instance.getGameVariables().getOnlineMode();
            boolean forceEnableMultiplayer = instance.getGameVariables().getForceEnableMultiplayer();

            StyledButton singlePlayer = new StyledButton("Single Player",   true);
            StyledButton multiPlayer  = new StyledButton("Multiplayer",     forceEnableMultiplayer || onlineMode);

            StyledButton colorBtn     = new StyledButton("Choose Color",    true);
            StyledButton exitBtn      = new StyledButton("Exit",            true);

            singlePlayer.addActionListener(e -> {
                instance.joinSinglePlayer();
            });

            multiPlayer.addActionListener(e -> {
                instance.getScreen().setScreenState(Screen.ScreenState.MULTIPLAYERMENU);
            });

            colorBtn.addActionListener(e -> showColorSelector(instance, instance.getScreen()));

            exitBtn.addActionListener(e -> System.exit(0));

            add(singlePlayer);
            add(multiPlayer);
            add(colorBtn);
            add(exitBtn);
        }

        // ── Layout buttons on resize ──────────────────────────────────────

        @Override
        public void doLayout() {
            super.doLayout();
            positionButtons();
        }

        private void positionButtons() {
            Component[] kids = getComponents();
            if (kids.length == 0) return;

            int btnW  = Math.min(320, getWidth() / 4);
            int btnH  = 52;
            int gap   = 16;
            int totalH = kids.length * btnH + (kids.length - 1) * gap;
            int startX = (getWidth()  - btnW) / 2;
            int startY = (getHeight() - totalH) / 2 + 40; // +40 to sit below title

            for (int i = 0; i < kids.length; i++) {
                kids[i].setBounds(startX, startY + i * (btnH + gap), btnW, btnH);
            }
        }

        // ── Background painting ───────────────────────────────────────────

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // Subtle radial vignette from centre
            RadialGradientPaint vignette = new RadialGradientPaint(
                    w / 2f, h / 2f,
                    Math.max(w, h) * 0.65f,
                    new float[]{ 0f, 1f },
                    new Color[]{ BG_MID, BG_DARK }
            );
            g2.setPaint(vignette);
            g2.fillRect(0, 0, w, h);

            // Horizontal rule below title
            int ruleY = h / 2 - 120;
            g2.setColor(SEPARATOR);
            g2.drawLine(w / 4, ruleY, 3 * w / 4, ruleY);

            // Title
            drawTitle(g2, w, ruleY);

            // Tiny version / mode tag bottom-right
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
            g2.setColor(TEXT_DIM);
            String modeTag = instance.getGameVariables().getOnlineMode()
                    ? "● Online" : "○ Offline";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(modeTag, w - fm.stringWidth(modeTag) - 20, h - 16);
        }

        private void drawTitle(Graphics2D g2, int w, int ruleY) {
            // Main title
            Font titleFont = g2.getFont().deriveFont(Font.BOLD, 42f);
            g2.setFont(titleFont);
            FontMetrics fm = g2.getFontMetrics();
            String title = "AMAZING GAME";
            int tx = (w - fm.stringWidth(title)) / 2;
            int ty = ruleY - 16;

            // Glow layer
            g2.setColor(new Color(100, 180, 255, 30));
            g2.drawString(title, tx + 2, ty + 2);

            // Main text
            g2.setColor(TEXT_PRIMARY);
            g2.drawString(title, tx, ty);

            // Accent underline dot row
            g2.setColor(ACCENT);
            int dotY  = ruleY - 4;
            int dotGap = 6;
            int dotCount = 5;
            int dotTotalW = dotCount * 4 + (dotCount - 1) * dotGap;
            int dotStartX = (w - dotTotalW) / 2;
            for (int i = 0; i < dotCount; i++) {
                g2.fillOval(dotStartX + i * (4 + dotGap), dotY, 4, 4);
            }

            // Subtitle
            Font subFont = g2.getFont().deriveFont(Font.PLAIN, 14f);
            g2.setFont(subFont);
            fm = g2.getFontMetrics();
            String sub = "Select an option to begin";
            g2.setColor(TEXT_DIM);
            g2.drawString(sub, (w - fm.stringWidth(sub)) / 2, ruleY + 20);
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    //  Custom painted button — matches PlayScene key/panel aesthetic
    // ─────────────────────────────────────────────────────────────────────────

    public static class StyledButton extends JComponent {

        private final String label;
        private boolean hovered  = false;
        private boolean pressed  = false;
        private final boolean enabled;

        StyledButton(String label, boolean enabled) {
            this.label   = label;
            this.enabled = enabled;
            setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());

            if (enabled) {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e)  { hovered = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)   { hovered = false; pressed = false; repaint(); }
                    @Override public void mousePressed(MouseEvent e)  { pressed = true;  repaint(); }
                    @Override public void mouseReleased(MouseEvent e) {
                        if (hovered && pressed) fireActionPerformed();
                        pressed = false;
                        repaint();
                    }
                });
            }
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int arc = 10;

            // Fill
            if (!enabled) {
                g2.setColor(BTN_DISABLED);
            } else if (pressed) {
                g2.setColor(ACCENT.darker());
            } else if (hovered) {
                g2.setColor(BTN_HOVER);
            } else {
                g2.setColor(BTN_NORMAL);
            }
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

            // Left accent bar when hovered/pressed
            if (enabled && (hovered || pressed)) {
                g2.setColor(pressed ? ACCENT_BRIGHT : ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, h * 0.2f, 3, h * 0.6f, 3, 3));
            }

            // Border
            g2.setColor(enabled && hovered ? ACCENT : BTN_BORDER);
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));

            // Label
            Font font = g2.getFont().deriveFont(Font.BOLD, 14f);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(label)) / 2;
            int ty = (h + fm.getAscent() - fm.getDescent()) / 2;

            if (!enabled) {
                g2.setColor(TEXT_DIM);
            } else if (pressed) {
                g2.setColor(Color.WHITE);
            } else if (hovered) {
                g2.setColor(ACCENT_BRIGHT);
            } else {
                g2.setColor(TEXT_PRIMARY);
            }
            g2.drawString(label, tx, ty);
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    //  Color selector (unchanged logic, kept here)
    // ─────────────────────────────────────────────────────────────────────────

    public static void showColorSelector(Game instance, Screen screen) {
        ColorSelector colorSelector = new ColorSelector(
                screen, instance.getGameVariables().getSelectedColor());
        instance.getGameVariables().setSelectedColor(colorSelector.getSelectedColor());
        screen.repaint();
    }
}