package net.wattpadpremium;

import java.awt.*;
import java.io.IOException;
import java.util.*;

public class GameServer {

    public final TCPServer tcpServer;
    private int goalX, goalY;
    private int mazeWidth = 15, mazeHeight = 15;
    private final HashMap<String , ServerPlayer> allPlayers = new HashMap<>();
    private int[][] maze;
    private final int maxPlayerCount = 2;

    private boolean matchStarted = false;

    public GameServer() throws IOException {
        tcpServer = new TCPServer();
        tcpServer.getServerPacketHandler().put(JoinPacket.ID, (packet, clientHandler) -> {
            if (matchStarted){
                return;
            }
            JoinPacket joinPacket = (JoinPacket) packet;
            if (joinPacket.getUsername().length() > 16){
                return;
            }
            ServerPlayer serverPlayer = new ServerPlayer(this, clientHandler, joinPacket.getUsername(), joinPacket.getColor());
            System.out.println("Player " + joinPacket.getUsername() +  " has joined the game " + allPlayers.size() + "/" + maxPlayerCount);
            if (allPlayers.size() == maxPlayerCount){
                startGame();
            }
            allPlayers.forEach((string, player) -> {
                PlayerCountPacket playerCountPacket = new PlayerCountPacket();
                playerCountPacket.setCount(allPlayers.size());
                playerCountPacket.setMax(maxPlayerCount);
                player.sendPacket(playerCountPacket);
            });
        });
        tcpServer.getServerPacketHandler().put(PositionChangePacket.ID, (packet, clientHandler) -> {
            ServerPlayer serverPlayer = clientHandler.getServerPlayer();
            PositionChangePacket positionChangePacket = (PositionChangePacket) packet;
            if (serverPlayer != null) {
                serverPlayer.setX(positionChangePacket.getX());
                serverPlayer.setY(positionChangePacket.getY());
                serverPlayer.setColor(positionChangePacket.getColor());
            }else {
                return;
            }
            System.out.println(positionChangePacket.getUsername() + " moved to " +positionChangePacket.getX() + " _ " + positionChangePacket.getY());

            for (ServerPlayer player : allPlayers.values()){
                if (player.getX() == goalX && player.getY() == goalY) {
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
        for (ServerPlayer player : allPlayers.values()){
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

        int spawnX;
        int spawnY;
        do {
            spawnX = random.nextInt(mazeWidth);
            spawnY = random.nextInt(mazeHeight);
            goalX = random.nextInt(mazeWidth);
            goalY = random.nextInt(mazeHeight);

        } while (maze[spawnY][spawnX] != 0 || maze[goalY][goalX] != 0);

        for (ServerPlayer player : allPlayers.values()){
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

    public void playerJoinEvent(ServerPlayer serverPlayer) {
        allPlayers.put(serverPlayer.getUsername(), serverPlayer);
    }

    public void playerQuitEvent(ServerPlayer serverPlayer){
        allPlayers.remove(serverPlayer.getUsername());
        //TODO send player remove packet
        if (allPlayers.size() != maxPlayerCount){
            endGame();
        }
    }

    private void startGame(){
        matchStarted = true;
        generateMap();
        broadcastMaze();
        sendPlayerPositions();
    }

    private void endGame() {
        matchStarted = false;
        tcpServer.broadcastPacket(new EndGamePacket());
    }
}
