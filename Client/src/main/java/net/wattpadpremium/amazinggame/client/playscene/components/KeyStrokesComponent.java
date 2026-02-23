package net.wattpadpremium.amazinggame.client.playscene.components;

import net.wattpadpremium.amazinggame.client.PlayScene;

import java.awt.*;

public class KeyStrokesComponent {


    // ─────────────────────────────────────────────────────────────────────────
    //  Left panel – keybind visualiser
    // ─────────────────────────────────────────────────────────────────────────

    public static void drawKeybindPanel(Graphics g, PlayScene playScene) {
        int panelW = playScene.leftPanelWidth();
        int panelH = playScene.getHeight();

        // Panel background
        g.setColor(new Color(20, 20, 25));
        g.fillRect(0, 0, panelW, panelH);

        // Right-edge separator
        g.setColor(new Color(60, 60, 70));
        g.drawLine(panelW - 1, 0, panelW - 1, panelH);

        // ── Title ─────────────────────────────────────────────────────────
        g.setColor(new Color(200, 200, 210));
        Font titleFont = g.getFont().deriveFont(Font.BOLD, 14f);
        g.setFont(titleFont);
        String title = "CONTROLS";
        FontMetrics tfm = g.getFontMetrics();
        g.drawString(title, (panelW - tfm.stringWidth(title)) / 2, 40);

        // ── D-pad ─────────────────────────────────────────────────────────
        // Key square size scales with the panel width
        int keySize = Math.max(24, Math.min(panelW / 5, 48));
        int gap     = Math.max(4, keySize / 5);

        // Vertical centre of the D-pad cluster (slightly above screen centre)
        int clusterCX = panelW / 2;
        int clusterCY = panelH / 2;

        // [col, row] offsets in key-units for each direction
        // UP(0), DOWN(1), LEFT(2), RIGHT(3)
        int[][] offsets = {
                {  0, -1 },   // UP
                {  0,  1 },   // DOWN
                { -1,  0 },   // LEFT
                {  1,  0 },   // RIGHT
        };
        String[] arrows  = { "▲", "▼", "◀", "▶" };
        boolean[] states = { playScene.keyState[0], playScene.keyState[1], playScene.keyState[2], playScene.keyState[3] };

        Font keyFont = g.getFont().deriveFont(Font.BOLD, keySize * 0.40f);
        g.setFont(keyFont);

        for (int i = 0; i < 4; i++) {
            int cx = clusterCX + offsets[i][0] * (keySize + gap);
            int cy = clusterCY + offsets[i][1] * (keySize + gap);
            drawKey(g, cx - keySize / 2, cy - keySize / 2, keySize, keySize,
                    arrows[i], states[i]);
        }

        // Centre dot between arrow keys (cosmetic)
        g.setColor(new Color(50, 50, 60));
        int dotR = keySize / 4;
        g.fillRoundRect(clusterCX - dotR, clusterCY - dotR, dotR * 2, dotR * 2, 4, 4);

        // ── TAB key ───────────────────────────────────────────────────────
        int tabW = keySize * 2 + gap;
        int tabH = keySize / 2 + 4;
        int tabX = clusterCX - tabW / 2;
        int tabY = clusterCY + (keySize + gap) + gap * 3;

        drawKey(g, tabX, tabY, tabW, tabH, "TAB  –  Scoreboard", playScene.keyState[4], 10f);

    }

    /** Draws a single key rectangle with label, auto-sizing the font if needed. */
    private static void drawKey(Graphics g, int x, int y, int w, int h,
                         String label, boolean pressed) {
        drawKey(g, x, y, w, h, label, pressed, -1f);
    }

    private static void drawKey(Graphics g, int x, int y, int w, int h,
                         String label, boolean pressed, float forceFontSize) {
        // Fill
        g.setColor(pressed ? new Color(100, 180, 255) : new Color(50, 50, 60));
        g.fillRoundRect(x, y, w, h, 8, 8);
        // Border
        g.setColor(pressed ? new Color(160, 210, 255) : new Color(80, 80, 95));
        g.drawRoundRect(x, y, w, h, 8, 8);
        // Label
        Font font = forceFontSize > 0
                ? g.getFont().deriveFont(Font.PLAIN, forceFontSize)
                : g.getFont();
        g.setFont(font);
        g.setColor(pressed ? Color.WHITE : new Color(140, 140, 160));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label,
                x + (w - fm.stringWidth(label)) / 2,
                y + (h + fm.getAscent()) / 2 - 2);
    }

}
