package net.wattpadpremium;

import java.awt.*;
import java.io.IOException;
import java.util.*;

public class GameServer {

    public final TCPServer tcpServer;
    private int spawnX, spawnY;
    private int goalX, goalY;
    private int mazeWidth = 15, mazeHeight = 15;
    private final HashMap<String , ServerPlayer> players = new HashMap<>();
    private int[][] maze;
    private final int maxPlayerCount = 2;

    private boolean matchStarted = false;

    public GameServer() throws IOException {
        tcpServer = new TCPServer();
        tcpServer.getPacketHandler().put(JoinPacket.ID, packet -> {
            if (matchStarted){
                return;
            }
            JoinPacket joinPacket = (JoinPacket) packet;
            ServerPlayer joiningPlayer = new ServerPlayer(joinPacket.getUsername(), joinPacket.getColor());
            players.putIfAbsent(joinPacket.getUsername(), joiningPlayer);
            System.out.println("Player " + joinPacket.getUsername() +  " has joined the game " + players.size() + "/" + maxPlayerCount);
            if (players.size() == maxPlayerCount){
                matchStarted = true;
                generateMap();
                broadcastMaze();
                sendPlayerPositions();
            }
        });
        tcpServer.getPacketHandler().put(PositionChangePacket.ID, packet -> {
            PositionChangePacket positionChangePacket = (PositionChangePacket) packet;
            ServerPlayer player = players.get(positionChangePacket.getUsername());
            if (player != null) {
                player.setX(positionChangePacket.getX());
                player.setY(positionChangePacket.getY());
                player.setColor(positionChangePacket.getColor());
            }else {
                return;
            }
            System.out.println(positionChangePacket.getUsername() + " moved to " +positionChangePacket.getX() + " _ " + positionChangePacket.getY());

            for (ServerPlayer serverPlayer : players.values()){
                if (serverPlayer.getX() == goalX && serverPlayer.getY() == goalY) {
                    serverPlayer.setScore(serverPlayer.getScore()+1);
                    mazeWidth += 2;
                    mazeHeight += 2;
                    generateMap();
                    broadcastMaze();
                    sendPlayerScore(serverPlayer);
                    break;
                }
            }

            sendPlayerPositions();

        });
        tcpServer.startServer();
    }

    private void sendPlayerScore(ServerPlayer serverPlayer) {
        PlayerScorePacket playerScorePacket = new PlayerScorePacket(serverPlayer.getUsername(),serverPlayer.getScore());
        tcpServer.broadcastPacket(playerScorePacket);
    }

    private void sendPlayerPositions() {
        for (ServerPlayer player : players.values()){
            PositionChangePacket positionChangePacket = new PositionChangePacket();
            positionChangePacket.setUsername(player.getUsername());
            positionChangePacket.setX(player.getX());
            positionChangePacket.setY(player.getY());
            positionChangePacket.setColor(player.getColor());
            tcpServer.broadcastPacket(positionChangePacket);
        }
    }

    private void broadcastMaze() {
        MazePacket mazePacket = new MazePacket();
        mazePacket.setMaze(maze);
        mazePacket.setGoalY(goalY);
        mazePacket.setGoalX(goalX);
        tcpServer.broadcastPacket(mazePacket);
    }

    public static void main(String[] args) throws IOException {
        new GameServer();
    }


    private void generateMap() {
        Random random = new Random();
        generateMazeUsingRecursiveBacktracking();

        do {
            spawnX = random.nextInt(mazeWidth);
            spawnY = random.nextInt(mazeHeight);


            goalX = random.nextInt(mazeWidth);
            goalY = random.nextInt(mazeHeight);

        } while (maze[spawnY][spawnX] != 0 || maze[goalY][goalX] != 0);

        for (ServerPlayer player : players.values()){
            player.setX(spawnX);
            player.setY(spawnY);
        }
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
}