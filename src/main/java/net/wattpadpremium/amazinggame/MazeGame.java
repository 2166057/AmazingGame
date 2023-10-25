package net.wattpadpremium.amazinggame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.util.Random;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

public class MazeGame extends JFrame {
    private int playerX, playerY;
    private int goalX, goalY;
    private int mazeWidth = 15;
    private int mazeHeight = 15;
    private final int cellSize = 30;
    private int viewPortX = 0;  // X-coordinate of the top-left corner of the viewport
    private int viewPortY = 0;  // Y-coordinate of the top-left corner of the viewport
    private final int viewPortWidth = 10;  // Number of visible cells in width
    private final int viewPortHeight = 10; // Number of visible cells in height
    private int[][] maze;

    private boolean[] keyState = new boolean[4]; // 0: UP, 1: DOWN, 2: LEFT, 3: RIGHT

    public MazeGame() {
        setTitle("Maze Game");
        setSize(viewPortWidth * cellSize, viewPortHeight * cellSize);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        addKeyListener(new KeyHandler());
        setLayout(null);

        generatePlayerAndGoalPositions();
    }

    @Override
    public void paint(Graphics g) {
        Image offScreenBuffer = createImage(getWidth(), getHeight());
        Graphics offScreenGraphics = offScreenBuffer.getGraphics();

        for (int x = 0; x < viewPortWidth; x++) {
            for (int y = 0; y < viewPortHeight; y++) {
                int cellX = viewPortX + x;
                int cellY = viewPortY + y;
                if (cellX < 0 || cellX >= mazeWidth || cellY < 0 || cellY >= mazeHeight) {
                    offScreenGraphics.setColor(Color.BLACK);
                    offScreenGraphics.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                } else {
                    int cell = maze[cellY][cellX];
                    offScreenGraphics.setColor(cell == 1 ? Color.BLACK : Color.WHITE);
                    offScreenGraphics.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                }
            }
        }

        offScreenGraphics.setColor(Color.RED);
        offScreenGraphics.fillOval((playerX - viewPortX) * cellSize, (playerY - viewPortY) * cellSize, cellSize, cellSize);

        offScreenGraphics.setColor(Color.GREEN);
        offScreenGraphics.fillRect((goalX - viewPortX) * cellSize, (goalY - viewPortY) * cellSize, cellSize, cellSize);

        g.drawImage(offScreenBuffer, 0, 0, this);
    }

    private void generateMazeUsingRecursiveBacktracking() {
        maze = new int[mazeHeight][mazeWidth]; // Create a new maze

        for (int x = 0; x < mazeWidth; x++) {
            for (int y = 0; y < mazeHeight; y++) {
                maze[y][x] = 1;
            }
        }

        Stack<Point> stack = new Stack<>();
        Random random = new Random();

        int startX = 2;
        int startY = 2;
        maze[startY][startX] = 0;

        stack.push(new Point(startX, startY));

        while (!stack.isEmpty()) {
            Point current = stack.peek();
            int x = current.x;
            int y = current.y;

            int[] dx = {2, 0, -2, 0};
            int[] dy = {0, 2, 0, -2};

            int[] randomOrder = {0, 1, 2, 3};
            randomOrder = shuffle(randomOrder, random);

            boolean deadEnd = true;
            for (int i = 0; i < 4; i++) {
                int r = randomOrder[i];
                int newX = x + dx[r];
                int newY = y + dy[r];

                if (newX > 0 && newX < mazeWidth - 1 && newY > 0 && newY < mazeHeight - 1 && maze[newY][newX] == 1) {
                    maze[newY][newX] = 0;
                    maze[y + dy[r] / 2][x + dx[r] / 2] = 0;
                    stack.push(new Point(newX, newY));
                    deadEnd = false;
                    break;
                }
            }

            if (deadEnd) {
                stack.pop();
            }
        }
    }

    private int[] shuffle(int[] array, Random random) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = array[i];
            array[i] = array[index];
            array[index] = temp;
        }
        return array;
    }

    private void generatePlayerAndGoalPositions() {
        Random random = new Random();
        generateMazeUsingRecursiveBacktracking();
        while (true) {
            playerX = random.nextInt(mazeWidth);
            playerY = random.nextInt(mazeHeight);
            goalX = random.nextInt(mazeWidth);
            goalY = random.nextInt(mazeHeight);

            if (maze[playerY][playerX] == 0 && maze[goalY][goalX] == 0) {
                break;
            }
        }
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
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MazeGame mazeGame = new MazeGame();
            mazeGame.setVisible(true);

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mazeGame.handleContinuousMovement();
                    mazeGame.repaint();
                }
            }, 0, 50); // Adjust the interval as needed
        });
    }

    private void handleContinuousMovement() {
        int dx = 0;
        int dy = 0;

        if (keyState[0]) dy = -1;
        if (keyState[1]) dy = 1;
        if (keyState[2]) dx = -1;
        if (keyState[3]) dx = 1;

        if (dx != 0 || dy != 0) {
            int newX = playerX + dx;
            int newY = playerY + dy;

            if (newX >= 0 && newX < mazeWidth && newY >= 0 && newY < mazeHeight && maze[newY][newX] != 1) {
                playerX = newX;
                playerY = newY;

                if (playerX == goalX && playerY == goalY) {
                    mazeWidth += 2; // Increase the maze width
                    mazeHeight += 2; // Increase the maze height
                    generatePlayerAndGoalPositions();
                }

                // Implement scrolling
                if (playerX - viewPortX < 2) {
                    viewPortX = Math.max(playerX - 2, 0);
                } else if (playerX - viewPortX > viewPortWidth - 3) {
                    viewPortX = Math.min(playerX - viewPortWidth + 3, mazeWidth - viewPortWidth);
                }
                if (playerY - viewPortY < 2) {
                    viewPortY = Math.max(playerY - 2, 0);
                } else if (playerY - viewPortY > viewPortHeight - 3) {
                    viewPortY = Math.min(playerY - viewPortHeight + 3, mazeHeight - viewPortHeight);
                }
            }
        }
    }
}
