package net.wattpadpremium.server;

import net.wattpadpremium.*;
import net.wattpadpremium.server.boxes.*;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GameServer implements GameServerAPI{

    public final TCPServer tcpServer;
    private int goalX, goalY;
    private int mazeWidth = 15, mazeHeight = 15;
    private final HashMap<String , ServerPlayer> allPlayers = new HashMap<>();

    private final HashMap<UUID, Trap> trapMap = new HashMap<>();

    private int spawnX = 0,spawnY = 0;
//    private final HashMap<String , Bot> bots = new HashMap<>();

    private int[][] maze;
    private final int maxPlayerCount = 3;
    private final int minPlayerSize = 1;
//    private int numberOfBots = 1;

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
            if (allPlayers.size() >= minPlayerSize){
                beginCountDown();
            }
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
            }else {
                return;
            }
//            System.out.println(positionChangePacket.getUsername() + " moved to " +positionChangePacket.getX() + " _ " + positionChangePacket.getY());

            if (serverPlayer.getX() == goalX && serverPlayer.getY() == goalY) {
                serverPlayer.setScore(serverPlayer.getScore()+1);
                mazeWidth += 2;
                mazeHeight += 2;
                generateMap();
                broadcastMaze();
            }


            List<UUID> trapsToRemove = new ArrayList<>();
            trapMap.forEach((uuid, trap) -> {
                if (trap.getPosX() == serverPlayer.getX() && trap.getPosY() == serverPlayer.getY()){
                    System.out.println(serverPlayer.getUsername() + " stepped on trap " + trap.getTrapUUID());
                    trap.onTrigger(this, serverPlayer);
                    if (trap.isDeleted()){
                        trapsToRemove.add(trap.getTrapUUID());
                    }
                }
            });

            trapsToRemove.forEach(uuid -> {
                removeTrap(trapMap.get(uuid));
            });

            broadcastPositionsToAll();
        });
        tcpServer.startServer();
    }

    private void beginCountDown() {
        new Thread(() -> {
            try {
                for (int i = 5; i > 0; i--) {
                    System.out.println("Game starts in " + i + " seconds...");
                    Thread.sleep(1000);
                }
                if (allPlayers.size() >= minPlayerSize && allPlayers.size() <= maxPlayerCount) {
                    startGame();
                } else {
                    System.out.println("Player count is not within range. Game cannot start.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void broadcastPositionsToAll() {
        for (ServerPlayer player : allPlayers.values()){
            notifyPositionChangeToClients(player);
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


        //Generate Traps
        Set<String> trapPositions = new HashSet<>();
        while (trapMap.size() < 10) {
            int posX = random.nextInt(mazeWidth);
            int posY = random.nextInt(mazeHeight);
            String key = posX + "," + posY;

            if (maze[posY][posX] == 0 && !trapPositions.contains(key)) {
                Trap trap;

                // Randomly select a trap type
                int trapType = new Random().nextInt(6); // 0, 1, 2 or 3
                switch (trapType) {
                    case 0:
                        trap = new RestartTrap(this, posX, posY);
                        break;
                    case 1:
                        trap = new DizzyTrap(this, posX, posY);
                        break;
                    case 2:
                        trap = new BlindnessTrap(this, posX, posY);
                        break;
                    case 3:
                        trap = new GhostBonus(this, posX, posY);
                        break;
                    case 4:
                        trap = new StatusTrap(this, posX, posY, PlayerStatusPacket.STATUS.INVISIBLE);
                        break;
                    case 5:
                        trap = new StatusTrap(this, posX, posY, PlayerStatusPacket.STATUS.FROZEN);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + trapType);
                }

                spawnTrap(trap);
                trapPositions.add(key);
            }

        }

        for (ServerPlayer player : allPlayers.values()){
            player.setX(spawnX);
            player.setY(spawnY);
            //resetPlayer status
            for (PlayerStatusPacket.STATUS status : PlayerStatusPacket.STATUS.values()){
                player.setStatus(status,false);
            }
        }

//        for (Bot bot : bots.values()){
//            bot.updateMaze(maze, spawnX, spawnY, goalX, goalY);
//        }



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
        tcpServer.broadcastPacket(new RemovePlayerPacket(serverPlayer.getUsername()));
        if (allPlayers.isEmpty()){
            endGame();
        }
    }

    private void startGame(){
        if (!matchStarted){
            matchStarted = true;
//            for (int amount = numberOfBots; amount > 0; amount--){
//                String botName = "bot"+amount;
//                Bot bot = new Bot(botName);
//                System.out.println("Adding bot :" + botName);
//                bots.put(botName, bot);
//            }
            generateMap();
            broadcastMaze();
            broadcastPositionsToAll();
        }
    }


    private void endGame() {
        matchStarted = false;
        tcpServer.broadcastPacket(new EndGamePacket());
    }

    @Override
    public int[][] getMaze() {
        return new int[0][];
    }

    @Override
    public int getGoalX() {
        return goalX;
    }

    @Override
    public int getGoalY() {
        return goalY;
    }

    @Override
    public ServerPlayer getServerPlayer(String username) {
        return allPlayers.get(username);
    }

    @Override
    public List<ServerPlayer> getServerPlayers() {
        return new ArrayList<>(allPlayers.values());
    }

    @Override
    public int getSpawnX() {
        return spawnX;
    }

    @Override
    public int getSpawnY() {
        return spawnY;
    }

    @Override
    public void notifyPositionChangeToClients(ServerPlayer serverPlayer) {
        PositionChangePacket positionChangePacket = new PositionChangePacket();
        positionChangePacket.setUsername(serverPlayer.getUsername());
        positionChangePacket.setX(serverPlayer.getX());
        positionChangePacket.setY(serverPlayer.getY());
        positionChangePacket.setColor(serverPlayer.getColor());
        tcpServer.broadcastPacket(positionChangePacket);
    }

    public void onVisibilityChange(Trap trap) {
        TrapPacket trapPacket;
        if (trap.isVisible()){
            trapPacket = new TrapPacket(trap.getTrapUUID().toString(), trap.getPosX(), trap.getPosY(), Color.ORANGE.getRGB(), false);
        }else {
            trapPacket = new TrapPacket(trap.getTrapUUID().toString(), trap.getPosX(), trap.getPosY(), Color.ORANGE.getRGB(), true);
        }
        tcpServer.broadcastPacket(trapPacket);
    }

    @Override
    public void removeTrap(Trap trap) {
        trapMap.remove(trap.getTrapUUID());
        if (!trap.isDeleted()){
            trap.setDeleted(true);
        }
        trap.changeVisibility(false);
    }

    @Override
    public void spawnTrap(Trap trap) {
        trapMap.put(trap.getTrapUUID(), trap);
    }
}
