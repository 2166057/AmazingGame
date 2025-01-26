package net.wattpadpremium.amazinggame.client;

import com.fasterxml.jackson.annotation.JsonFormat;
import net.wattpadpremium.MazePacket;
import net.wattpadpremium.PlayerStatusPacket;
import net.wattpadpremium.PositionChangePacket;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.*;

public class GameScene extends JFrame {
    private final TCPClient tcpClient;
    private int localPosX = 0, localPosY = 0;
    private final HashMap<String, Player> otherPlayers = new HashMap<>();
    private int goalX = 0, goalY = 0;
    private int mazeSize = 15;
    private final int cellSize = 30;
    private int viewPortX = 0;  // X-coordinate of the top-left corner of the viewport
    private int viewPortY = 0;  // Y-coordinate of the top-left corner of the viewport
    private final int viewPortWidth = 16*3;  // Number of visible cells in width
    private final int viewPortHeight = 9*3; // Number of visible cells in height
    private int[][] maze;

    private final HashMap<PlayerStatusPacket.STATUS, Boolean> statusMap = new HashMap<>();

    private final HashMap<UUID, ClientObject> drawables = new HashMap<>();

    private Image goalImage;

    Timer ticking;

    private final boolean[] keyState = new boolean[5]; // 0: UP, 1: DOWN, 2: LEFT, 3: RIGHT, 4: TAB

    private final GameInstance gameInstance;
    private int score = 0;

    public GameScene(TCPClient tcpClient, GameInstance gameInstance, MazePacket mazePacket) {
        this.tcpClient = tcpClient;
        this.gameInstance = gameInstance;

        for (PlayerStatusPacket.STATUS value : PlayerStatusPacket.STATUS.values()) {
            statusMap.put(value, false);
        }

        try {
            goalImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/goal.png")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setFocusTraversalKeysEnabled(false);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                tcpClient.stopClient();
            }
        });

        updateMaze(mazePacket);
        setSize(viewPortWidth * cellSize, viewPortHeight * cellSize);
        setResizable(false);
        addKeyListener(new KeyHandler());
        setVisible(true);
        localPosX = 0;
        localPosY = 0;

        ticking = new Timer();
        ticking.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handleContinuousMovement();
                repaint();
            }
        }, 0, 50);
    }

    @Override
    public void paint(Graphics g) {
        Image offScreenBuffer = createImage(getWidth(), getHeight());
        Graphics offScreenGraphics = offScreenBuffer.getGraphics();

        //maze
        for (int x = 0; x < viewPortWidth; x++) {
            for (int y = 0; y < viewPortHeight; y++) {
                int cellX = viewPortX + x;
                int cellY = viewPortY + y;
                if (cellX < 0 || cellX >= mazeSize || cellY < 0 || cellY >= mazeSize) {
                    offScreenGraphics.setColor(Color.BLACK);
                    offScreenGraphics.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                } else {
                    int cell = maze[cellY][cellX];

                    boolean isBlinded = isStatusActive(PlayerStatusPacket.STATUS.BLINDED);

                    Color normalCellColor = isBlinded ? Color.BLACK : getBackground();
                    Color wallCellColor = isStatusActive(PlayerStatusPacket.STATUS.GHOSTING) && !isBlinded  ? Color.DARK_GRAY : Color.BLACK;

                    offScreenGraphics.setColor(cell == 1 ? wallCellColor : normalCellColor);
                    offScreenGraphics.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                }
            }
        }

        // Render the traps
        for (ClientObject drawable : drawables.values()) {
            int trapX = drawable.x - viewPortX;
            int trapY = drawable.y - viewPortY;

            if (trapX >= 0 && trapX < viewPortWidth && trapY >= 0 && trapY < viewPortHeight) {
                offScreenGraphics.setColor(drawable.color);
                offScreenGraphics.fillRoundRect(
                        trapX * cellSize,
                        trapY * cellSize,
                        cellSize,
                        cellSize,
                        cellSize / 4,
                        cellSize / 4
                );
            }
        }

        //self
        boolean invisible = isStatusActive(PlayerStatusPacket.STATUS.INVISIBLE);
        boolean frozen = isStatusActive(PlayerStatusPacket.STATUS.FROZEN);

        Color playerColor = gameInstance.getProfile().getColor();

        if (frozen) {
            playerColor = new Color(173, 216, 230);
        }

        if (frozen) {
            offScreenGraphics.setColor(Color.WHITE);
            offScreenGraphics.fillOval((localPosX - viewPortX) * cellSize - 5, (localPosY - viewPortY) * cellSize - 5, cellSize + 10, cellSize + 10); // White outline
        }

        if (!invisible) {
            offScreenGraphics.setColor(playerColor);
            offScreenGraphics.fillOval((localPosX - viewPortX) * cellSize, (localPosY - viewPortY) * cellSize, cellSize, cellSize);
        }

        //server_players
        for (Player player : otherPlayers.values()){
            offScreenGraphics.setColor(player.getColor());
            offScreenGraphics.fillOval((player.getX() - viewPortX) * cellSize, (player.getY() - viewPortY) * cellSize, cellSize, cellSize);
            offScreenGraphics.setColor(Color.GRAY);
            offScreenGraphics.drawString(player.getUsername(),(player.getX() - viewPortX) * cellSize, (player.getY() - viewPortY) * cellSize);
        }


        //goal
        offScreenGraphics.drawImage(
                goalImage,
                (goalX - viewPortX) * cellSize,
                (goalY - viewPortY) * cellSize,
                cellSize,
                cellSize,
                null // ImageObserver, can be null if not needed
        );


        if (keyState[4]) {
            int canvasWidth = getWidth();
            int canvasHeight = getHeight();

            if (canvasWidth <= 0 || canvasHeight <= 0) {
                return;
            }

            List<Player> copy = new ArrayList<>(otherPlayers.values());

            Player localPlayer = new Player();
            localPlayer.setUsername(gameInstance.getProfile().getUsername());
            localPlayer.setScore(score);
            copy.add(localPlayer);

            copy.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));

            int rectWidth = 200;
            int rectHeight = 150;

            int rectX = (canvasWidth - rectWidth) / 2;
            int rectY = (canvasHeight - rectHeight) / 2;

            offScreenGraphics.setColor(Color.LIGHT_GRAY);
            offScreenGraphics.fillRect(rectX, rectY, rectWidth, rectHeight);

            offScreenGraphics.setColor(Color.BLACK);
            int lineHeight = offScreenGraphics.getFontMetrics().getHeight();
            int textX = rectX + 10;
            int textY = rectY + lineHeight;

            for (int i = 0; i < Math.min(3, copy.size()); i++) {
                Player player = copy.get(i);
                String playerDisplay = "#" + (i + 1) + " " + player.getUsername() + " - " + player.getScore();
                offScreenGraphics.drawString(playerDisplay, textX, textY);
                textY += lineHeight;
            }

            String localPlayerDisplay = "Your Score: " + score;
            offScreenGraphics.drawString(localPlayerDisplay, textX, textY);
        }



        g.drawImage(offScreenBuffer, 0, 0, this);
    }

    public void setLocalePosition(int x, int y) {
        localPosX = x;
        localPosY = y;
    }

    public void setSpecificPlayerPos(String username, int x, int y) {
        Player player = otherPlayers.get(username);
        if (player != null){
            player.setX(x);
            player.setY(y);
        }else {
            Player newPlayer = new Player() ;
            newPlayer.setUsername(username);
            newPlayer.setX(x);
            newPlayer.setY(y);
            otherPlayers.putIfAbsent(username, newPlayer);
        }
    }

    public void changeSpecificPlayerColor(String username, int color) {
        Player player = otherPlayers.get(username);
        if (player != null){
            player.setColor(new Color(color));
        }else {
            Player newPlayer = new Player() ;
            newPlayer.setUsername(username);
            newPlayer.setColor(new Color(color));
            otherPlayers.putIfAbsent(username, newPlayer);
        }
    }

    public void updateMaze(MazePacket mazePacket) {
        this.maze = mazePacket.getMaze();
        this.mazeSize = mazePacket.getMaze().length;
        this.goalX = mazePacket.getGoalX();
        this.goalY = mazePacket.getGoalY();
    }

    public void setMyScore(int score) {
        this.score = score;
    }

    public void changeSpecificPlayerScore(String username, int score) {
        Player player = otherPlayers.get(username);
        if (player != null){
            player.setScore(score);
        }
    }

    public void removePlayer(String username) {
        otherPlayers.remove(username);
    }

    public void addDrawable(ClientObject clientObject) {
        drawables.put(clientObject.objectUUID, clientObject);
    }

    public void removeDrawable(UUID trapID) {
        drawables.remove(trapID);
    }

    public void setLocalePlayerStatus(PlayerStatusPacket.STATUS status, boolean enabled) {
        statusMap.put(status, enabled);
    }

    private class KeyHandler extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_UP) {
                keyState[0] = true;
            } else if (keyCode == KeyEvent.VK_DOWN) {
                keyState[1] = true;
            } else if (keyCode == KeyEvent.VK_LEFT) {
                keyState[2] = true;
            } else if (keyCode == KeyEvent.VK_RIGHT) {
                keyState[3] = true;
            } else if (keyCode == KeyEvent.VK_TAB) {
                keyState[4] = true;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_UP) {
                keyState[0] = false;
            } else if (keyCode == KeyEvent.VK_DOWN) {
                keyState[1] = false;
            } else if (keyCode == KeyEvent.VK_LEFT) {
                keyState[2] = false;
            } else if (keyCode == KeyEvent.VK_RIGHT) {
                keyState[3] = false;
            } else if (keyCode == KeyEvent.VK_TAB){
                keyState[4] = false;
            }
        }
    }

    public Boolean isStatusActive(PlayerStatusPacket.STATUS status){
        return statusMap.get(status);
    }


    public void handleContinuousMovement() {
        if (isStatusActive(PlayerStatusPacket.STATUS.FROZEN)){
            return;
        }

        int dx = 0;
        int dy = 0;

        if (keyState[0]) dy = -1;
        if (keyState[1]) dy = 1;
        if (keyState[2]) dx = -1;
        if (keyState[3]) dx = 1;

        if (isStatusActive(PlayerStatusPacket.STATUS.DIZZY)) {
            dx = -dx;
            dy = -dy;
        }

        if (dx != 0 || dy != 0) {
            int newX = localPosX + dx;
            int newY = localPosY + dy;

            boolean isGhost = isStatusActive(PlayerStatusPacket.STATUS.GHOSTING);

            if (newX >= 2 && newX < mazeSize-2 && newY >= 2 && newY < mazeSize-2) {
                if (isGhost || maze[newY][newX] != 1) {
                    setLocalePosition(newX, newY);
                    sendPositionChanges();

                    if (localPosX - viewPortX < 2) {
                        viewPortX = Math.max(localPosX - 2, 0);
                    } else if (localPosX - viewPortX > viewPortWidth - 3) {
                        viewPortX = Math.min(localPosX - viewPortWidth + 3, mazeSize - viewPortWidth);
                    }
                    if (localPosY - viewPortY < 2) {
                        viewPortY = Math.max(localPosY - 2, 0);
                    } else if (localPosY - viewPortY > viewPortHeight - 3) {
                        viewPortY = Math.min(localPosY - viewPortHeight + 3, mazeSize - viewPortHeight);
                    }
                }
            }
        }

    }

    private void sendPositionChanges() {
        PositionChangePacket packet = new PositionChangePacket();
        packet.setUsername(gameInstance.getProfile().getUsername());
        packet.setColor(gameInstance.getProfile().getColor().getRGB());
        packet.setY(localPosY);
        packet.setX(localPosX);
        tcpClient.sendPacket(packet);
    }

    public void endGame(){
        ticking.cancel();
        this.setVisible(false);
        this.dispose();
    }
}
