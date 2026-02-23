package net.wattpadpremium.amazinggame.client;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class ColorSelector extends JDialog {

    // ── Palette (matches PlayScene / GameMenu / MultiplayerMenu) ─────────────
    private static final Color BG_DARK       = new Color(20, 20, 25);
    private static final Color BG_MID        = new Color(30, 30, 35);
    private static final Color SEPARATOR     = new Color(60, 60, 70);
    private static final Color TEXT_PRIMARY  = new Color(200, 200, 210);
    private static final Color TEXT_DIM      = new Color(100, 100, 115);
    private static final Color ACCENT        = new Color(100, 180, 255);
    private static final Color ACCENT_BRIGHT = new Color(160, 210, 255);
    private static final Color BTN_NORMAL    = new Color(50, 50, 60);
    private static final Color BTN_HOVER     = new Color(65, 65, 80);
    private static final Color BTN_BORDER    = new Color(80, 80, 95);

    @Getter
    private Color selectedColor;

    // Hue-Saturation-Brightness state
    private float hue        = 0f;
    private float saturation = 1f;
    private float brightness = 1f;

    // Rendered gradient images (rebuilt on resize)
    private BufferedImage hueBarImage;
    private BufferedImage svImage;

    // Component bounds (computed in layout)
    private final Rectangle svRect  = new Rectangle();
    private final Rectangle hueRect = new Rectangle();

    // Preview swatch rect
    private final Rectangle previewRect = new Rectangle();

    public ColorSelector(JFrame parent, Color initialColor) {
        super(parent, "Choose Color", true);
        selectedColor = initialColor != null ? initialColor : Color.RED;

        // Decompose initial color into HSB
        float[] hsb = Color.RGBtoHSB(
                selectedColor.getRed(),
                selectedColor.getGreen(),
                selectedColor.getBlue(), null);
        hue        = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];

        setUndecorated(true);
        setBackground(BG_DARK);

        int dialogW = 460;
        int dialogH = 420;
        setSize(dialogW, dialogH);
        setLocationRelativeTo(parent);
        setResizable(false);

        ColorPickerPanel panel = new ColorPickerPanel();
        setContentPane(panel);
        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Main panel
    // ─────────────────────────────────────────────────────────────────────────

    private class ColorPickerPanel extends JPanel {

        private final StyledButton okButton     = new StyledButton("OK",     true);
        private final StyledButton cancelButton = new StyledButton("Cancel", false);

        ColorPickerPanel() {
            setLayout(null);
            setBackground(BG_DARK);

            add(okButton);
            add(cancelButton);

            okButton.addActionListener(e -> {
                selectedColor = Color.getHSBColor(hue, saturation, brightness);
                dispose();
            });
            cancelButton.addActionListener(e -> dispose());

            // Mouse interaction on SV square and hue bar
            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e)  { handleMouse(e); }
                @Override public void mouseDragged(MouseEvent e)  { handleMouse(e); }
            };
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        private void handleMouse(MouseEvent e) {
            int mx = e.getX(), my = e.getY();

            if (svRect.contains(mx, my)) {
                saturation = (float)(mx - svRect.x) / svRect.width;
                brightness = 1f - (float)(my - svRect.y) / svRect.height;
                saturation = Math.max(0f, Math.min(1f, saturation));
                brightness = Math.max(0f, Math.min(1f, brightness));
                repaint();
            } else if (hueRect.contains(mx, my)) {
                hue = (float)(mx - hueRect.x) / hueRect.width;
                hue = Math.max(0f, Math.min(1f, hue));
                svImage = null; // force rebuild
                repaint();
            }
        }

        @Override
        public void doLayout() {
            super.doLayout();
            int w = getWidth(), h = getHeight();

            int pad    = 20;
            int titleH = 32;                        // space reserved for title bar
            int svSize = h - titleH - pad * 2       // fill most of the height
                    - 18                     // hue bar height
                    - 12                     // gap between sv and hue
                    - 12;                    // bottom padding

            // Cap sv square so it doesn't exceed available width minus right panel
            int rightColW = 110;
            int maxSvW    = w - pad * 3 - rightColW;
            svSize = Math.min(svSize, maxSvW);

            int svTop  = titleH + pad;

            svRect.setBounds(pad, svTop, svSize, svSize);
            hueRect.setBounds(pad, svTop + svSize + 12, svSize, 18);

            // Right column: preview swatch → hex label gap → OK → Cancel
            int rightX    = pad + svSize + pad;
            int rightW    = w - rightX - pad;

            // Square preview — make it fit with room for hex + two buttons below
            int btnH      = 38;
            int hexLabelH = 20;
            int innerGap  = 10;
            int previewH  = rightW; // keep it square
            // If that's too tall, shrink it
            int available = svSize + 18 + 12; // same total height as left column
            int neededBelow = hexLabelH + innerGap + btnH + innerGap + btnH + innerGap;
            if (previewH + neededBelow > available) {
                previewH = available - neededBelow;
            }
            previewH = Math.max(previewH, 40);

            previewRect.setBounds(rightX, svTop, rightW, previewH);

            int y = svTop + previewH + hexLabelH + innerGap + 4;
            okButton.setBounds(rightX, y, rightW, btnH);
            y += btnH + innerGap;
            cancelButton.setBounds(rightX, y, rightW, btnH);

            svImage     = null;
            hueBarImage = null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // Background
            g2.setColor(BG_DARK);
            g2.fillRect(0, 0, w, h);

            // Border around dialog
            g2.setColor(SEPARATOR);
            g2.drawRect(0, 0, w - 1, h - 1);

            // ── Title ─────────────────────────────────────────────────────
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
            g2.setColor(TEXT_PRIMARY);
            g2.drawString("CHOOSE COLOR", 20, 22);

            g2.setColor(SEPARATOR);
            g2.drawLine(0, 30, w, 30);

            // ── SV square ─────────────────────────────────────────────────
            if (svImage == null || svImage.getWidth() != svRect.width) {
                svImage = buildSVImage(svRect.width, svRect.height);
            }
            g2.drawImage(svImage, svRect.x, svRect.y, null);

            // SV border
            g2.setColor(SEPARATOR);
            g2.drawRect(svRect.x, svRect.y, svRect.width, svRect.height);

            // SV cursor crosshair
            int curX = svRect.x + (int)(saturation * svRect.width);
            int curY = svRect.y + (int)((1f - brightness) * svRect.height);
            g2.setColor(Color.WHITE);
            g2.drawOval(curX - 5, curY - 5, 10, 10);
            g2.setColor(Color.BLACK);
            g2.drawOval(curX - 6, curY - 6, 12, 12);

            // ── Hue bar ───────────────────────────────────────────────────
            if (hueBarImage == null || hueBarImage.getWidth() != hueRect.width) {
                hueBarImage = buildHueBar(hueRect.width, hueRect.height);
            }
            g2.drawImage(hueBarImage, hueRect.x, hueRect.y, null);
            g2.setColor(SEPARATOR);
            g2.drawRect(hueRect.x, hueRect.y, hueRect.width, hueRect.height);

            // Hue cursor
            int hueX = hueRect.x + (int)(hue * hueRect.width);
            g2.setColor(Color.WHITE);
            g2.fillRect(hueX - 2, hueRect.y - 3, 4, hueRect.height + 6);
            g2.setColor(Color.BLACK);
            g2.drawRect(hueX - 3, hueRect.y - 4, 5, hueRect.height + 7);

            // ── Preview swatch ────────────────────────────────────────────
            Color current = Color.getHSBColor(hue, saturation, brightness);

            // Checkerboard behind preview (shows transparency context)
            paintCheckerboard(g2, previewRect);

            g2.setColor(current);
            g2.fill(new RoundRectangle2D.Float(
                    previewRect.x, previewRect.y,
                    previewRect.width, previewRect.height, 8, 8));
            g2.setColor(SEPARATOR);
            g2.draw(new RoundRectangle2D.Float(
                    previewRect.x, previewRect.y,
                    previewRect.width, previewRect.height, 8, 8));

            // Hex label below preview
            String hex = String.format("#%02X%02X%02X",
                    current.getRed(), current.getGreen(), current.getBlue());
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 11f));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(TEXT_DIM);
            g2.drawString(hex,
                    previewRect.x + (previewRect.width - fm.stringWidth(hex)) / 2,
                    previewRect.y + previewRect.height + fm.getAscent() + 4);
        }

        // ── Image builders ────────────────────────────────────────────────

        private BufferedImage buildSVImage(int w, int h) {
            if (w <= 0 || h <= 0) return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    float s = (float) x / w;
                    float b = 1f - (float) y / h;
                    img.setRGB(x, y, Color.HSBtoRGB(hue, s, b));
                }
            }
            return img;
        }

        private BufferedImage buildHueBar(int w, int h) {
            if (w <= 0 || h <= 0) return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < w; x++) {
                int rgb = Color.HSBtoRGB((float) x / w, 1f, 1f);
                for (int y = 0; y < h; y++) {
                    img.setRGB(x, y, rgb);
                }
            }
            return img;
        }

        private void paintCheckerboard(Graphics2D g2, Rectangle r) {
            int size = 6;
            for (int x = r.x; x < r.x + r.width; x += size) {
                for (int y = r.y; y < r.y + r.height; y += size) {
                    boolean light = ((x / size + y / size) % 2 == 0);
                    g2.setColor(light ? new Color(60, 60, 65) : new Color(40, 40, 45));
                    g2.fillRect(x, y,
                            Math.min(size, r.x + r.width  - x),
                            Math.min(size, r.y + r.height - y));
                }
            }
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    //  Styled button (same as GameMenu / MultiplayerMenu)
    // ─────────────────────────────────────────────────────────────────────────

    private static class StyledButton extends JComponent {

        private final String  label;
        private final boolean isPrimary;
        private       boolean hovered = false;
        private       boolean pressed = false;

        StyledButton(String label, boolean isPrimary) {
            this.label     = label;
            this.isPrimary = isPrimary;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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

            Color fill = pressed ? ACCENT.darker()
                    : hovered ? BTN_HOVER
                    : isPrimary ? BTN_NORMAL
                    : new Color(40, 40, 50);

            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 10, 10));

            if (hovered || pressed) {
                g2.setColor(pressed ? ACCENT_BRIGHT : ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, h * 0.2f, 3, h * 0.6f, 3, 3));
            }

            g2.setColor(hovered ? ACCENT : BTN_BORDER);
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 10, 10));

            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 13f));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(pressed ? Color.WHITE : hovered ? ACCENT_BRIGHT : TEXT_PRIMARY);
            g2.drawString(label,
                    (w - fm.stringWidth(label)) / 2,
                    (h + fm.getAscent() - fm.getDescent()) / 2);
        }
    }
}