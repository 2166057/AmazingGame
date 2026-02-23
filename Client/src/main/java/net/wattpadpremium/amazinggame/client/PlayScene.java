package net.wattpadpremium.amazinggame.client;

import lombok.Getter;
import lombok.Setter;
import net.wattpadpremium.amazinggame.client.playscene.components.ScoreboardComponent;
import net.wattpadpremium.amazinggame.client.tcp.AbstractTCPClient;
import net.wattpadpremium.client.MovePacket;
import net.wattpadpremium.server.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Timer;
import java.util.*;

import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;
import static net.wattpadpremium.amazinggame.client.playscene.components.KeyStrokesComponent.drawKeybindPanel;

public class PlayScene extends JPanel {

    private final Game instance;

    @Getter
    @Setter
    private Player localPlayer;

    private int localPosX = 0, localPosY = 0;
    private final HashMap<Long, Player> otherPlayers = new HashMap<>();
    private int goalX = 0, goalY = 0;
    private int mazeSize = 15;
    private int[][] maze;

    private int viewPortX = 0;
    private int viewPortY = 0;

    private static final int MIN_CELL_SIZE = 10;
    private static final int MAX_CELL_SIZE = 60;
    private static final double MAZE_MAX_RATIO  = 3.0 / 5.0;
    private static final double LEFT_PANEL_RATIO = 1.0 / 5.0;

    private final HashMap<PlayerStatusPacket.STATUS, Boolean> statusMap = new HashMap<>();
    private final HashMap<UUID, ClientObject> drawables = new HashMap<>();

    private final Image goalImage;
    private Timer ticking;

    // 0:UP  1:DOWN  2:LEFT  3:RIGHT  4:TAB
    public final boolean[] keyState = new boolean[5];

    private int score = 0;

    // ─────────────────────────────────────────────────────────────────────────
    //  Text overlay state
    // ─────────────────────────────────────────────────────────────────────────

    private String    overlayText      = null;
    private long      overlayExpireAt  = 0;
    private TimerTask overlayClearTask = null;

    // ─────────────────────────────────────────────────────────────────────────
    //  Progress bar state
    // ─────────────────────────────────────────────────────────────────────────

    private boolean progressBarVisible = true;
    private String  progressBarText    = "";
    private Color   progressBarColor   = new Color(100, 180, 255);
    private int     progressBarValue   = 0; // 0–100


    // ─────────────────────────────────────────────────────────────────────────
    //  Layout helpers
    // ─────────────────────────────────────────────────────────────────────────

    public int leftPanelWidth() {
        return (int) (getWidth() * LEFT_PANEL_RATIO);
    }

    private int computeCellSize() {
        int areaW = getWidth() - leftPanelWidth();
        int areaH = getHeight();
        if (areaW <= 0 || areaH <= 0 || mazeSize <= 0) return MIN_CELL_SIZE;
        int maxPxW   = (int) (getWidth()  * MAZE_MAX_RATIO);
        int maxPxH   = (int) (getHeight() * MAZE_MAX_RATIO);
        int cellByW  = areaW  / mazeSize;
        int cellByH  = areaH  / mazeSize;
        int cellCapW = maxPxW / mazeSize;
        int cellCapH = maxPxH / mazeSize;
        int cell = Math.min(Math.min(cellByW, cellByH), Math.min(cellCapW, cellCapH));
        return Math.max(MIN_CELL_SIZE, Math.min(cell, MAX_CELL_SIZE));
    }

    private int visibleCellsX(int cellSize) {
        return Math.min(mazeSize, (int) (getWidth() * MAZE_MAX_RATIO) / cellSize);
    }

    private int visibleCellsY(int cellSize) {
        return Math.min(mazeSize, (int) (getHeight() * MAZE_MAX_RATIO) / cellSize);
    }

    private int mazeOriginX(int cellSize) {
        int areaW = getWidth() - leftPanelWidth();
        return leftPanelWidth() + (areaW - visibleCellsX(cellSize) * cellSize) / 2;
    }

    private int mazeOriginY(int cellSize) {
        return (getHeight() - visibleCellsY(cellSize) * cellSize) / 2;
    }

    private static final int SCROLL_MARGIN = 3;

    private void updateViewport(int cellSize) {
        int visX = visibleCellsX(cellSize);
        int visY = visibleCellsY(cellSize);
        if (localPosX - viewPortX < SCROLL_MARGIN)
            viewPortX = localPosX - SCROLL_MARGIN;
        else if (localPosX - viewPortX > visX - 1 - SCROLL_MARGIN)
            viewPortX = localPosX - visX + 1 + SCROLL_MARGIN;
        if (localPosY - viewPortY < SCROLL_MARGIN)
            viewPortY = localPosY - SCROLL_MARGIN;
        else if (localPosY - viewPortY > visY - 1 - SCROLL_MARGIN)
            viewPortY = localPosY - visY + 1 + SCROLL_MARGIN;
        viewPortX = Math.max(0, Math.min(viewPortX, mazeSize - visX));
        viewPortY = Math.max(0, Math.min(viewPortY, mazeSize - visY));
    }



    // ─────────────────────────────────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public PlayScene(Game instance) {
        this.instance = instance;

        for (PlayerStatusPacket.STATUS v : PlayerStatusPacket.STATUS.values()) {
            statusMap.put(v, false);
        }

        try {
            goalImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/goal.png")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        addKeyListener(new KeyHandler());

    }


    // ─────────────────────────────────────────────────────────────────────────
    //  Rendering
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void paintComponent(Graphics g) {
        Image    buf = createImage(getWidth(), getHeight());
        Graphics og  = buf.getGraphics();

        og.setColor(new Color(30, 30, 35));
        og.fillRect(0, 0, getWidth(), getHeight());

        drawKeybindPanel(og, this);

        // Status indicators drawn to og (the buffer), not g
        int panelW    = leftPanelWidth();
        int panelH    = getHeight();
        int keySize   = Math.max(24, Math.min(panelW / 5, 48));
        int gap       = Math.max(4, keySize / 5);
        int clusterCY = panelH / 2;
        drawStatusIndicators(og, panelW, 60, clusterCY - (keySize + gap) - gap * 4);

        int cellSize = computeCellSize();
        int visX     = visibleCellsX(cellSize);
        int visY     = visibleCellsY(cellSize);
        int originX  = mazeOriginX(cellSize);
        int originY  = mazeOriginY(cellSize);

        updateViewport(cellSize);

        if (maze != null) {
            boolean isBlinded  = isStatusActive(PlayerStatusPacket.STATUS.BLINDED);
            boolean isGhosting = isStatusActive(PlayerStatusPacket.STATUS.GHOSTING);
            Color normalColor  = isBlinded ? Color.BLACK : getBackground();
            Color wallColor    = (isGhosting && !isBlinded) ? Color.DARK_GRAY : Color.BLACK;

            for (int dy = 0; dy < visY; dy++) {
                for (int dx = 0; dx < visX; dx++) {
                    int cellX = viewPortX + dx;
                    int cellY = viewPortY + dy;
                    if (cellX < 0 || cellX >= mazeSize || cellY < 0 || cellY >= mazeSize)
                        og.setColor(Color.BLACK);
                    else
                        og.setColor(maze[cellY][cellX] == 1 ? wallColor : normalColor);
                    og.fillRect(originX + dx * cellSize, originY + dy * cellSize, cellSize, cellSize);
                }
            }

            for (Player p : otherPlayers.values()) {
                int relX = p.getX() - viewPortX, relY = p.getY() - viewPortY;
                if (relX >= 0 && relX < visX && relY >= 0 && relY < visY) {
                    og.setColor(p.getColor());
                    og.fillOval(originX + relX * cellSize, originY + relY * cellSize, cellSize, cellSize);
                }
            }

            boolean invisible = isStatusActive(PlayerStatusPacket.STATUS.INVISIBLE);
            boolean frozen    = isStatusActive(PlayerStatusPacket.STATUS.FROZEN);
            int relLX = localPosX - viewPortX, relLY = localPosY - viewPortY;
            if (relLX >= 0 && relLX < visX && relLY >= 0 && relLY < visY) {
                int px = originX + relLX * cellSize, py = originY + relLY * cellSize;
                if (frozen) { og.setColor(Color.WHITE); og.fillOval(px - 5, py - 5, cellSize + 10, cellSize + 10); }
                if (!invisible) {
                    og.setColor(frozen ? new Color(173, 216, 230) : localPlayer.getColor());
                    og.fillOval(px, py, cellSize, cellSize);
                }
            }

            for (ClientObject obj : drawables.values()) {
                int relX = obj.x - viewPortX, relY = obj.y - viewPortY;
                if (relX >= 0 && relX < visX && relY >= 0 && relY < visY) {
                    og.setColor(obj.color);
                    int px = originX + relX * cellSize, py = originY + relY * cellSize;
                    og.fillRoundRect(px, py, cellSize, cellSize, cellSize / 4, cellSize / 4);
                }
            }

            int relGX = goalX - viewPortX, relGY = goalY - viewPortY;
            if (relGX >= 0 && relGX < visX && relGY >= 0 && relGY < visY)
                og.drawImage(goalImage, originX + relGX * cellSize, originY + relGY * cellSize,
                        cellSize, cellSize, null);

        } else {
            og.setColor(Color.WHITE);
            String msg = "Loading Terrain...";
            FontMetrics fm = og.getFontMetrics();
            og.drawString(msg,
                    leftPanelWidth() + (getWidth() - leftPanelWidth() - fm.stringWidth(msg)) / 2,
                    getHeight() / 2);
        }

        // ── Overlays drawn last so they sit on top of everything ──────────
        if (progressBarVisible)  drawProgressBar(og);
        if (hasActiveOverlay())  drawTextOverlay(og);
        if (keyState[4]) ScoreboardComponent.drawScoreboard(og, otherPlayers, localPlayer, score, getWidth(), getHeight());

        g.drawImage(buf, 0, 0, this);
    }


    // ─────────────────────────────────────────────────────────────────────────
    //  Progress bar  (Minecraft XP-bar style)
    // ─────────────────────────────────────────────────────────────────────────


    private void drawProgressBar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ── Dimensions ────────────────────────────────────────────────────
        int barW  = (int) (getWidth() * MAZE_MAX_RATIO);   // matches maze width cap
        int barH  = 18;
        int barX  = leftPanelWidth() + (getWidth() - leftPanelWidth() - barW) / 2;
        int barY  = getHeight() - barH - 20;               // 20 px above the bottom edge

        int clamp = Math.max(0, Math.min(100, progressBarValue));
        int fillW = (int) (barW * (clamp / 100.0));

        // ── Track (dark tint of the fill color) ───────────────────────────
        g2.setColor(progressBarColor.darker().darker());
        g2.fillRoundRect(barX, barY, barW, barH, 6, 6);

        // ── Filled portion ────────────────────────────────────────────────
        if (fillW > 0) {
            g2.setColor(progressBarColor);
            g2.fillRoundRect(barX, barY, fillW, barH, 6, 6);

            // Subtle highlight stripe along the top (depth effect)
            g2.setColor(new Color(255, 255, 255, 50));
            g2.fillRoundRect(barX, barY, fillW, barH / 2, 6, 6);
        }

        // ── Segment notches every 10% ─────────────────────────────────────
        g2.setColor(new Color(0, 0, 0, 80));
        for (int seg = 1; seg < 10; seg++) {
            int notchX = barX + (barW * seg / 10);
            g2.fillRect(notchX, barY, 2, barH);
        }

        // ── Outer border ──────────────────────────────────────────────────
        g2.setColor(new Color(80, 80, 95));
        g2.drawRoundRect(barX, barY, barW, barH, 6, 6);

        // ── Centered label ────────────────────────────────────────────────
        if (progressBarText != null && !progressBarText.isEmpty()) {
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 11f));
            FontMetrics fm = g2.getFontMetrics();
            int tx = barX + (barW - fm.stringWidth(progressBarText)) / 2;
            int ty = barY + (barH + fm.getAscent() - fm.getDescent()) / 2;

            // Shadow
            g2.setColor(new Color(0, 0, 0, 160));
            g2.drawString(progressBarText, tx + 1, ty + 1);
            // Label — white so it's readable over any fill color
            g2.setColor(Color.WHITE);
            g2.drawString(progressBarText, tx, ty);
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    //  Status indicators
    // ─────────────────────────────────────────────────────────────────────────

    private void drawStatusIndicators(Graphics g, int panelW,
                                      int availableTop, int availableBottom) {
        PlayerStatusPacket.STATUS[] statuses = PlayerStatusPacket.STATUS.values();
        if (statuses.length == 0) return;

        float fontSize  = 11f;
        Font statusFont = g.getFont().deriveFont(Font.PLAIN, fontSize);
        g.setFont(statusFont);
        FontMetrics fm  = g.getFontMetrics();
        int lineH       = fm.getHeight() + 3;
        int totalH      = availableBottom - availableTop;
        int neededH     = lineH * (statuses.length + 2);

        while (neededH > totalH && fontSize > 7f) {
            fontSize   -= 0.5f;
            statusFont  = g.getFont().deriveFont(Font.PLAIN, fontSize);
            g.setFont(statusFont);
            fm          = g.getFontMetrics();
            lineH       = fm.getHeight() + 2;
            neededH     = lineH * (statuses.length + 2);
        }

        int y = availableTop + (totalH - neededH) / 2 + fm.getAscent();

        String header = "─── STATUS ───";
        g.setColor(new Color(90, 90, 110));
        g.drawString(header, (panelW - fm.stringWidth(header)) / 2, y);
        y += lineH + 2;

        for (PlayerStatusPacket.STATUS status : statuses) {
            boolean active = Boolean.TRUE.equals(statusMap.get(status));
            g.setColor(active ? new Color(255, 160, 80) : new Color(70, 70, 85));
            String lbl = formatStatus(status) + (active ? "  ●" : "  ○");
            g.drawString(lbl, (panelW - fm.stringWidth(lbl)) / 2, y);
            y += lineH;
        }
    }

    private String formatStatus(PlayerStatusPacket.STATUS status) {
        StringBuilder sb = new StringBuilder();
        for (String word : status.name().split("_")) {
            if (!word.isEmpty())
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase()).append(' ');
        }
        return sb.toString().trim();
    }


    // ─────────────────────────────────────────────────────────────────────────
    //  Text overlay
    // ─────────────────────────────────────────────────────────────────────────

    public void showOverlay(String text, long durationMs) {
        if (overlayClearTask != null) { overlayClearTask.cancel(); overlayClearTask = null; }
        overlayText     = text;
        overlayExpireAt = System.currentTimeMillis() + durationMs;
        overlayClearTask = new TimerTask() {
            @Override public void run() { overlayText = null; overlayExpireAt = 0; repaint(); }
        };
        if (ticking != null) ticking.schedule(overlayClearTask, durationMs);
    }

    private boolean hasActiveOverlay() {
        boolean active = overlayText != null && System.currentTimeMillis() < overlayExpireAt;
        if (overlayText != null)
            System.out.println("[overlay] text=" + overlayText
                    + " remaining=" + (overlayExpireAt - System.currentTimeMillis())
                    + " active=" + active);
        return active;
    }

    private void drawTextOverlay(Graphics g) {
        if (overlayText == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        long remaining = overlayExpireAt - System.currentTimeMillis();
        float alpha = remaining < 500 ? Math.max(0f, remaining / 500f) : 1.0f;

        int mazeAreaCX = leftPanelWidth() + (getWidth() - leftPanelWidth()) / 2;
        int mazeAreaCY = getHeight() / 2;

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 22f));
        FontMetrics fm = g2.getFontMetrics();
        String[] lines = overlayText.split("\n");
        int lineH = fm.getHeight() + 4;
        int maxW  = 0;
        for (String l : lines) maxW = Math.max(maxW, fm.stringWidth(l));

        int padX = 28, padY = 18;
        int pillW = maxW + padX * 2, pillH = lines.length * lineH + padY * 2;
        int pillX = mazeAreaCX - pillW / 2, pillY = mazeAreaCY - pillH / 2 - 40;

        Composite orig = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(pillX - 4, pillY - 4, pillW + 8, pillH + 8, 18, 18);
        g2.setColor(new Color(25, 25, 32));
        g2.fillRoundRect(pillX, pillY, pillW, pillH, 14, 14);
        g2.setColor(new Color(100, 180, 255, 180));
        g2.drawRoundRect(pillX, pillY, pillW, pillH, 14, 14);
        g2.setColor(new Color(100, 180, 255));
        g2.fillRoundRect(pillX + pillW / 2 - 20, pillY - 2, 40, 4, 4, 4);

        int textY = pillY + padY + fm.getAscent();
        for (String line : lines) {
            int lx = mazeAreaCX - fm.stringWidth(line) / 2;
            g2.setColor(new Color(0, 0, 0, 120)); g2.drawString(line, lx + 1, textY + 1);
            g2.setColor(new Color(200, 200, 210)); g2.drawString(line, lx, textY);
            textY += lineH;
        }
        g2.setComposite(orig);
    }


    // ─────────────────────────────────────────────────────────────────────────
    //  State helpers
    // ─────────────────────────────────────────────────────────────────────────

    public void setLocalePosition(int x, int y)              { localPosX = x; localPosY = y; }
    public void setMyScore(int score)                        { this.score = score; }
    public void removePlayer(Long playerId)                  { otherPlayers.remove(playerId); }
    public void addDrawable(ClientObject obj)                { drawables.put(obj.objectUUID, obj); }
    public void removeDrawable(UUID trapID)                  { drawables.remove(trapID); }
    public Boolean isStatusActive(PlayerStatusPacket.STATUS s) { return statusMap.get(s); }
    public void setLocalePlayerStatus(PlayerStatusPacket.STATUS s, boolean enabled) { statusMap.put(s, enabled); }

    public void setSpecificPlayerPos(Long playerId, int x, int y) {
        Player p = otherPlayers.get(playerId);
        if (p != null) { p.setX(x); p.setY(y); }
        else { Player np = new Player(); np.setPlayerId(playerId); np.setX(x); np.setY(y); otherPlayers.putIfAbsent(playerId, np); }
    }

    public void changeSpecificPlayerColor(Long playerId, int color) {
        Player p = otherPlayers.get(playerId);
        if (p != null) { p.setColor(new Color(color)); }
        else { Player np = new Player(); np.setPlayerId(playerId); np.setColor(new Color(color)); otherPlayers.putIfAbsent(playerId, np); }
    }

    public void updateMaze(MazePacket mazePacket) {
        this.maze     = mazePacket.getMaze();
        this.mazeSize = mazePacket.getMaze().length;
        this.goalX    = mazePacket.getGoalX();
        this.goalY    = mazePacket.getGoalY();
    }

    public void changeSpecificPlayerScore(long playerId, int score) {
        Player p = otherPlayers.get(playerId);
        if (p != null) p.setScore(score);
    }


    // ─────────────────────────────────────────────────────────────────────────
    //  Movement
    // ─────────────────────────────────────────────────────────────────────────

    public void handleContinuousMovement() {
        if (isStatusActive(PlayerStatusPacket.STATUS.FROZEN)) return;

        int dx = 0, dy = 0;
        if (keyState[0]) dy = -1;
        if (keyState[1]) dy =  1;
        if (keyState[2]) dx = -1;
        if (keyState[3]) dx =  1;

        if (isStatusActive(PlayerStatusPacket.STATUS.DIZZY)) { dx = -dx; dy = -dy; }
        if (dx == 0 && dy == 0) return;

        int newX = localPosX + dx, newY = localPosY + dy;
        System.out.println("previous pos " + localPosX + "," + localPosY);
        System.out.println("new pos " + newX + "," + newY);

        boolean isGhost = isStatusActive(PlayerStatusPacket.STATUS.GHOSTING);
        if (newX >= 2 && newX < mazeSize - 2 && newY >= 2 && newY < mazeSize - 2) {
            if (isGhost || maze[newY][newX] != 1) {
                setLocalePosition(newX, newY);
                sendPositionChanges();
            }
        }
    }

    private void sendPositionChanges() {
        MovePacket packet = new MovePacket();
        packet.setX(localPosX);
        packet.setY(localPosY);
        instance.getTcpClient().sendPacketToServer(packet);
    }


    // ─────────────────────────────────────────────────────────────────────────
    //  Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    public void disconnect() {
        instance.getTcpClient().stopClient();
        this.setVisible(false);
        this.instance.getScreen().setScreenState(Screen.ScreenState.MAINMENU);
    }

    public void startTicking() {
        stopTicking();
        ticking = new Timer();
        ticking.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() { handleContinuousMovement(); repaint(); }
        }, 0, 50);
    }

    private void stopTicking() {
        if (ticking != null) ticking.cancel();
    }


    // ─────────────────────────────────────────────────────────────────────────
    //  Key handling
    // ─────────────────────────────────────────────────────────────────────────

    private class KeyHandler extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP    -> keyState[0] = true;
                case KeyEvent.VK_DOWN  -> keyState[1] = true;
                case KeyEvent.VK_LEFT  -> keyState[2] = true;
                case KeyEvent.VK_RIGHT -> keyState[3] = true;
                case KeyEvent.VK_TAB   -> keyState[4] = true;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP    -> keyState[0] = false;
                case KeyEvent.VK_DOWN  -> keyState[1] = false;
                case KeyEvent.VK_LEFT  -> keyState[2] = false;
                case KeyEvent.VK_RIGHT -> keyState[3] = false;
                case KeyEvent.VK_TAB   -> keyState[4] = false;
            }
        }
    }


    public void configureClientPacketListener(AbstractTCPClient client) {
        client.getPacketHandler().put(AcceptConnectionPacket.ID, packet -> {

            AcceptConnectionPacket p = (AcceptConnectionPacket) packet;
            localPlayer = new Player();
            localPlayer.setUsername(p.getUsername());
            localPlayer.setPlayerId(p.getPlayerId());
            localPlayer.setColor(instance.getGameVariables().getSelectedColor());
            instance.getScreen().setScreenState(Screen.ScreenState.GAMEPLAY);
        });

        // ── ProgressBarPacket
        client.getPacketHandler().put(ProgressBarPacket.ID, packet -> {
            ProgressBarPacket p = (ProgressBarPacket) packet;
            progressBarVisible = p.isVisible();
            progressBarText    = p.getText();
            progressBarColor   = new Color(p.getColor());
            progressBarValue   = p.getProgress(); // 0–100
        });

        client.getPacketHandler().put(MazePacket.ID, packet ->
                updateMaze((MazePacket) packet));
        client.getPacketHandler().put(PositionChangePacket.ID, packet -> {
            PositionChangePacket p = (PositionChangePacket) packet;
            System.out.println("Received position changed " + p);
            if (p.getPlayerId() == localPlayer.getPlayerId()) {
                setLocalePosition(p.getX(), p.getY());
            } else {
                setSpecificPlayerPos(p.getPlayerId(), p.getX(), p.getY());
                changeSpecificPlayerColor(p.getPlayerId(), p.getColor());
            }
        });
        client.getPacketHandler().put(RemovePlayerPacket.ID, packet ->
                removePlayer(((RemovePlayerPacket) packet).getPlayedId()));
        client.getPacketHandler().put(PlayerScorePacket.ID, packet -> {
            PlayerScorePacket p = (PlayerScorePacket) packet;
            if (p.getPlayerId() == localPlayer.getPlayerId()) setMyScore(p.getScore());
            else changeSpecificPlayerScore(p.getPlayerId(), p.getScore());
        });
        client.getPacketHandler().put(EndGamePacket.ID, packet -> { });
        client.getPacketHandler().put(TextOverlayPacket.ID, packet -> {
            TextOverlayPacket p = (TextOverlayPacket) packet;
            System.out.println("[TextOverlay] text=" + p.getText() + " duration=" + p.getDurationMS());
            showOverlay(p.getText(), p.getDurationMS());
        });
        client.getPacketHandler().put(PlayerCountPacket.ID, packet -> { /* unused */ });
        client.getPacketHandler().put(TrapPacket.ID, packet -> {
            TrapPacket p   = (TrapPacket) packet;
            UUID uid = UUID.fromString(p.getTrapID());
            if (p.isDelete()) removeDrawable(uid);
            else addDrawable(new ClientObject(
                    p.getPosX(), p.getPosY(), new Color(p.getColor()), uid));
        });
        client.getPacketHandler().put(PlayerStatusPacket.ID, packet -> {
            PlayerStatusPacket p = (PlayerStatusPacket) packet;
            if (localPlayer.getPlayerId() == p.getPlayerId())
                setLocalePlayerStatus(p.getStatus(), p.isEnabled());
        });
    }
}