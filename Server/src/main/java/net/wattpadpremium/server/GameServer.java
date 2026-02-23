package net.wattpadpremium.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.wattpadpremium.*;
import net.wattpadpremium.client.AuthSessionPacket;
import net.wattpadpremium.client.MovePacket;
import net.wattpadpremium.server.boxes.*;
import net.wattpadpremium.server.modes.GameMode;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GameServer extends AbstractGameServer implements GameServerAPI {

    private ExtendableTimer timer;

    private final GameMode gameMode;

    private int goalX, goalY;
    private int mazeWidth = 15, mazeHeight = 15;
    private final HashMap<Long , ServerPlayer> allPlayers = new HashMap<>();

    private final HashMap<UUID, Trap> trapMap = new HashMap<>();

    private final boolean onlineMode;

    private int spawnX = 0,spawnY = 0;

    private int[][] maze;

    private boolean matchStarted = false;

    private boolean preventMovement = true;

    public GameServer() throws IOException {
        this(GameMode.SCORELIMIT);
    }

    public GameServer(GameMode gameMode) throws IOException {
        this(gameMode,false, new TCPServer(12345));
    }

    public GameServer(GameMode mode, boolean onlineMode, AbstractTCPServer tcpServer) {
        super(tcpServer);
        this.gameMode = mode;
        this.onlineMode = onlineMode;
        getTcpServer().getServerPacketHandler().put(AuthSessionPacket.ID, ((packet, clientHandler) -> {
            AuthSessionPacket authSessionPacket = (AuthSessionPacket) packet;
            try {
                AcceptConnectionPacket acceptConnectionPacket = new AcceptConnectionPacket();

                String jsonString = null;
                JsonElement jsonElement= null;
                if (onlineMode){
                    jsonString = SessionManager.validateSession(authSessionPacket.getSessionToken(),"*");
                    jsonElement = new Gson().fromJson(jsonString, JsonElement.class);
                }

                String username = this.onlineMode ? jsonElement.getAsJsonObject().get("username").getAsString() : authSessionPacket.getUsername();
                long id = this.onlineMode ? jsonElement.getAsJsonObject().get("Id").getAsLong() : new Random().nextLong();
                acceptConnectionPacket.setUsername(username);
                acceptConnectionPacket.setPlayerId(id);

                if (matchStarted){
                    return;
                }

                clientHandler.setServerPlayer(new ServerPlayer(this, clientHandler, id, username, Color.orange.getRGB()));
                clientHandler.getServerPlayer().onConnectMatch();
                clientHandler.sendPacketToClient(acceptConnectionPacket);
                System.out.println(acceptConnectionPacket);
                System.out.println("Player " + clientHandler.getServerPlayer().getUsername() +  " has joined the game " + allPlayers.size() + "/" + gameMode.getMaxPlayer());

                if (allPlayers.size() >= gameMode.getMinPlayer()){
                    beginCountDown();
                }
                if (allPlayers.size() == gameMode.getMaxPlayer()){
                    startGame();
                }

                allPlayers.forEach((string, player) -> {
                    PlayerCountPacket playerCountPacket = new PlayerCountPacket();
                    playerCountPacket.setCount(allPlayers.size());
                    playerCountPacket.setMax(gameMode.getMaxPlayer());
                    player.sendPacketToClient(playerCountPacket);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        getTcpServer().getServerPacketHandler().put(MovePacket.ID, (packet, clientHandler) -> {
            ServerPlayer serverPlayer = clientHandler.getServerPlayer();
            MovePacket movePacket = (MovePacket) packet;

            if (serverPlayer != null) {
                if (preventMovement){
                    clientHandler.sendPacketToClient(new PositionChangePacket(serverPlayer.getPlayerId(), serverPlayer.getX(),serverPlayer.getY(), serverPlayer.getColor()));
                    return;
                }
                serverPlayer.setX(movePacket.getX());
                serverPlayer.setY(movePacket.getY());
            }else {
                return;
            }

            if (serverPlayer.getX() == goalX && serverPlayer.getY() == goalY) {
                serverPlayer.setScore(serverPlayer.getScore()+1);
                if (GameMode.TIMER == gameMode){
                    timer.addSeconds(10);
                }
                if (GameMode.SCORELIMIT == gameMode && serverPlayer.getScore() == 2) {
                    var textOverlayPacket = new TextOverlayPacket(serverPlayer.getUsername() + " has won the game!", 3000);
                    getTcpServer().broadcastPacket(textOverlayPacket);
                    endGame();
                    return;
                }

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

            trapsToRemove.forEach(uuid -> removeTrap(trapMap.get(uuid)));

            broadcastPositionsToAll();
        });
        getTcpServer().startServer();
    }

    private void beginCountDown() {
        new Thread(() -> {
            try {
                for (int i = 5; i > 0; i--) {
                    System.out.println("Game starts in " + i + " seconds...");
                    if (!matchStarted){
                        getTcpServer().broadcastPacket(new TextOverlayPacket("Waiting for players " + i + "s", 1000));
                        Thread.sleep(1000);
                    }
                }
                if (allPlayers.size() >= gameMode.getMinPlayer() && allPlayers.size() <= gameMode.getMaxPlayer()) {
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
        getTcpServer().broadcastPacket(mazePacket);
    }

    public static void main(String[] args) throws IOException {
        new GameServer();
    }


    private void generateMap() {
        long start = System.nanoTime();

        Random random = new Random();
        generateMazeUsingRecursiveBacktracking();

        do {
            spawnX = random.nextInt(mazeWidth);
            spawnY = random.nextInt(mazeHeight);
            goalX  = random.nextInt(mazeWidth);
            goalY  = random.nextInt(mazeHeight);
        } while (maze[spawnY][spawnX] != 0 || maze[goalY][goalX] != 0);

        Set<String> trapPositions = new HashSet<>();
        while (trapMap.size() < 10) {
            int posX = random.nextInt(mazeWidth);
            int posY = random.nextInt(mazeHeight);
            String key = posX + "," + posY;

            if (maze[posY][posX] == 0 && !trapPositions.contains(key)) {
                int trapType = new Random().nextInt(6);
                Trap trap = switch (trapType) {
                    case 0 -> new RestartTrap(this, posX, posY);
                    case 1 -> new DizzyTrap(this, posX, posY);
                    case 2 -> new BlindnessTrap(this, posX, posY);
                    case 3 -> new GhostBonus(this, posX, posY);
                    case 4 -> new StatusTrap(this, posX, posY, PlayerStatusPacket.STATUS.INVISIBLE);
                    case 5 -> new StatusTrap(this, posX, posY, PlayerStatusPacket.STATUS.FROZEN);
                    default -> throw new IllegalStateException("Unexpected value: " + trapType);
                };
                spawnTrap(trap);
                trapPositions.add(key);
            }
        }

        for (ServerPlayer player : allPlayers.values()) {
            player.setX(spawnX);
            player.setY(spawnY);
            for (PlayerStatusPacket.STATUS status : PlayerStatusPacket.STATUS.values()) {
                player.setStatus(status, false);
            }
        }

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("[generateMap] mazeSize=%dx%d  took %d ms%n",
                mazeWidth, mazeHeight, elapsedMs);
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

    @Override
    public void playerJoinEvent(ServerPlayer serverPlayer) {
        allPlayers.put(serverPlayer.getPlayerId(), serverPlayer);
    }

    public void playerQuitEvent(ServerPlayer serverPlayer){
        getTcpServer().broadcastPacket(new RemovePlayerPacket(serverPlayer.getPlayerId()));
        allPlayers.remove(serverPlayer.getPlayerId());
        if (allPlayers.isEmpty()){
            endGame();
        }
    }

    private void startGame(){
        if (!matchStarted){
            matchStarted = true;

            preventMovement = false;
            generateMap();
            broadcastMaze();
            broadcastPositionsToAll();
            getTcpServer().broadcastPacket(new TextOverlayPacket("BEGIN!", 2000));
            if (gameMode == GameMode.TIMER){
                timer = new ExtendableTimer(60, ()->{
                    var packet = new ProgressBarPacket();
                    int progress = 0;
                    packet.setVisible(false);
                    packet.setProgress(progress);
                    packet.setColor(Color.GREEN.getRGB());
                    packet.setText("");
                    getTcpServer().broadcastPacket(packet);

                    var player = getServerPlayers().getFirst();

                    var matchEndingPacket = new TextOverlayPacket("Game Over! your score is " + player.getScore(), 5000);
                    getTcpServer().broadcastPacket(matchEndingPacket);
                    endGame();
                },
                        (remainingTime->{
                    var packet = new ProgressBarPacket();
                    packet.setVisible(true);
                    int progress = (int) ((remainingTime / 60.0) * 100);
                    packet.setProgress(progress);
                    packet.setColor(Color.GREEN.getRGB());
                    packet.setText("Time Remaining: " + remainingTime + "s");
                    getTcpServer().broadcastPacket(packet);
                }));
                timer.start();
            }


        }
    }


    private void endGame() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        matchStarted = false;
        preventMovement = true;
        getTcpServer().broadcastPacket(new EndGamePacket());
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
        return allPlayers.values().stream().filter(serverPlayer -> serverPlayer.getUsername().equalsIgnoreCase(username)).findFirst().get();
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
        positionChangePacket.setPlayerId(serverPlayer.getPlayerId());
        positionChangePacket.setX(serverPlayer.getX());
        positionChangePacket.setY(serverPlayer.getY());
        positionChangePacket.setColor(serverPlayer.getColor());
        getTcpServer().broadcastPacket(positionChangePacket);
    }

    public void onVisibilityChange(Trap trap) {
        TrapPacket trapPacket;
        if (trap.isVisible()){
            trapPacket = new TrapPacket(trap.getTrapUUID().toString(), trap.getPosX(), trap.getPosY(), Color.ORANGE.getRGB(), false);
        }else {
            trapPacket = new TrapPacket(trap.getTrapUUID().toString(), trap.getPosX(), trap.getPosY(), Color.ORANGE.getRGB(), true);
        }
        getTcpServer().broadcastPacket(trapPacket);
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
