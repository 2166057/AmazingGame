package net.wattpadpremium.server;

import lombok.Getter;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Bot {

    public Bot(String name){
        this.name = name;
    }

    @Getter
    private int posX, posY;
    private int[][] maze;
    public static final int movePerSeconds = 3;

    @Getter
    private final String name;

    public void moveUp() {
        posY--;
    }

    public void moveDown() {
        posY++;
    }

    public void moveLeft() {
        posX--;
    }

    public void moveRight() {
        posX++;
    }

    public void updateMaze(int[][] maze, int posX, int posY, int goalX, int goalY) {
        this.maze = maze;
        this.posX = posX;
        this.posY = posY;
        beginSolving(goalX, goalY);
    }

    private void beginSolving(int goalX, int goalY) {
        List<Point> path = findShortestPath(new Point(posX, posY), new Point(goalX, goalY));
        if (path == null) {
            System.out.println("No path found to the goal!");
            return;
        }

        new Thread(() -> {
            try {
                for (Point step : path) {
                    if (step.x > posX) moveRight();
                    else if (step.x < posX) moveLeft();
                    else if (step.y > posY) moveDown();
                    else if (step.y < posY) moveUp();

                    // Wait before the next move
                    Thread.sleep(1000 / movePerSeconds);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Maze solving interrupted!");
            }
        }).start();
    }

    private List<Point> findShortestPath(Point start, Point goal) {
        int[] dx = {1, 0, -1, 0};
        int[] dy = {0, 1, 0, -1};

        boolean[][] visited = new boolean[maze.length][maze[0].length];
        Point[][] previous = new Point[maze.length][maze[0].length];

        Queue<Point> queue = new LinkedList<>();
        queue.add(start);
        visited[start.y][start.x] = true;

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            if (current.equals(goal)) {
                return reconstructPath(previous, start, goal);
            }

            for (int i = 0; i < 4; i++) {
                int newX = current.x + dx[i];
                int newY = current.y + dy[i];

                if (isInBounds(newX, newY) && maze[newY][newX] == 0 && !visited[newY][newX]) {
                    visited[newY][newX] = true;
                    queue.add(new Point(newX, newY));
                    previous[newY][newX] = current;
                }
            }
        }

        return null; // No path found
    }

    private List<Point> reconstructPath(Point[][] previous, Point start, Point goal) {
        LinkedList<Point> path = new LinkedList<>();
        for (Point at = goal; at != null; at = previous[at.y][at.x]) {
            path.addFirst(at);
        }
        return path.getFirst().equals(start) ? path : null;
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < maze[0].length && y < maze.length;
    }

}
