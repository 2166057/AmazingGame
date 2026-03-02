package net.wattpadpremium.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.Getter;
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
    private int mazeWidth = 11, mazeHeight = 11;

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
        getTcpServer().getServerPacketHandler().put(PacketType.ClientAuthSessionPacket, ((packet, clientHandler) -> {
            AuthSessionPacket.Data packetData = (AuthSessionPacket.Data) packet.getData();
            try {
                String username;
                long playerId;

                String jsonString = null;
                JsonElement jsonElement= null;
                if (onlineMode){
                    jsonString = SessionManager.validateSession(packetData.sessionToken(),"*");
                    jsonElement = new Gson().fromJson(jsonString, JsonElement.class);
                }

                username = this.onlineMode ? jsonElement.getAsJsonObject().get("username").getAsString() : packetData.username();
                playerId = this.onlineMode ? jsonElement.getAsJsonObject().get("Id").getAsLong() : new Random().nextLong();

                if (matchStarted){
                    return;
                }

                clientHandler.setServerPlayer(new ServerPlayer(this, clientHandler, playerId, username, Color.orange.getRGB()));
                clientHandler.getServerPlayer().onConnectMatch();

                clientHandler.sendPacketToClient(new AcceptConnectionPacket(new AcceptConnectionPacket.Data(username, playerId)));
                System.out.println("Player " + clientHandler.getServerPlayer().getUsername() +  " has joined the game " + allPlayers.size() + "/" + gameMode.getMaxPlayer());

                if (allPlayers.size() >= gameMode.getMinPlayer()){
                    beginCountDown();
                }
                if (allPlayers.size() == gameMode.getMaxPlayer()){
                    startGame();
                }

                getTcpServer().broadcastPacket(new PlayerCountPacket(new PlayerCountPacket.Data(allPlayers.size(), gameMode.getMaxPlayer())));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        getTcpServer().getServerPacketHandler().put(PacketType.ClientMovePacket, (packet, clientHandler) -> {
            ServerPlayer serverPlayer = clientHandler.getServerPlayer();

            MovePacket.Data packetData = (MovePacket.Data) packet.getData();

            if (serverPlayer != null) {
                if (preventMovement){
                    clientHandler.sendPacketToClient(new PositionChangePacket(new PositionChangePacket.Data(serverPlayer.getPlayerId(), serverPlayer.getX(),serverPlayer.getY(), serverPlayer.getColor())));
                    return;
                }
                serverPlayer.setX(packetData.x());
                serverPlayer.setY(packetData.y());
            }else {
                return;
            }

            if (serverPlayer.getX() == goalX && serverPlayer.getY() == goalY) {
                serverPlayer.setScore(serverPlayer.getScore()+1);
                if (GameMode.TIMER == gameMode){
                    timer.addSeconds(10);
                }
                if (GameMode.SCORELIMIT == gameMode && serverPlayer.getScore() == 2) {
                    var textOverlayPacket = new TextOverlayPacket(new TextOverlayPacket.Data(serverPlayer.getUsername() + " has won the game!", 3000));
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

    private void broadcastRandomTheme() {
        enum Themes {
            DEFAULT(Color.WHITE, Color.BLACK),
            OCEAN(new Color(28, 107, 160), new Color(10, 45, 85)),
            SUNSET(new Color(255, 94, 77), new Color(102, 0, 51)),
            FOREST(new Color(34, 139, 34), new Color(0, 100, 0)),
            DESERT(new Color(237, 201, 175), new Color(194, 178, 128)),
            NIGHT(new Color(25, 25, 112), new Color(0, 0, 64)),
            FIRE(new Color(255, 69, 0), new Color(139, 0, 0)),
            ICE(new Color(173, 216, 230), new Color(0, 191, 255)),
            LAVENDER(new Color(230, 230, 250), new Color(138, 43, 226)),
            PINK(new Color(255, 182, 193), new Color(199, 21, 133)),
            CYAN(new Color(0, 255, 255), new Color(0, 139, 139)),
            MAGENTA(new Color(255, 0, 255), new Color(139, 0, 139)),
            GRAY(new Color(211, 211, 211), new Color(105, 105, 105)),
            TEAL(new Color(0, 128, 128), new Color(0, 77, 77)),
            PURPLE(new Color(128, 0, 128), new Color(75, 0, 130)),
            LIGHTGREEN(new Color(144, 238, 144), new Color(34, 139, 34)),
            DARKBLUE(new Color(0, 0, 139), new Color(0, 0, 80));

            @Getter
            private final Color back, wall;

            Themes(Color back, Color wall) {
                this.back = back;
                this.wall = wall;
            }
        }

        // Pick a random theme
        Themes[] themes = Themes.values();
        Themes theme = themes[(int) (Math.random() * themes.length)];

        // Broadcast the selected theme
        getTcpServer().broadcastPacket(
                new MazeStylePacket(
                        new MazeStylePacket.Data(
                                theme.getBack().getRGB(),
                                theme.getWall().getRGB()
                        )
                )
        );
    }

    private void beginCountDown() {
        new Thread(() -> {
            try {
                for (int i = 5; i > 0; i--) {
                    System.out.println("Game starts in " + i + " seconds...");
                    if (!matchStarted){

                        getTcpServer().broadcastPacket(new TextOverlayPacket(new TextOverlayPacket.Data("Waiting for players " + i + "s", 1000)));
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
        broadcastRandomTheme();
        getTcpServer().broadcastPacket(new MazePacket(new MazePacket.Data(maze, goalX, goalY)));
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
        getTcpServer().broadcastPacket(new RemovePlayerPacket(new RemovePlayerPacket.Data(serverPlayer.getPlayerId())));
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
            getTcpServer().broadcastPacket(new TextOverlayPacket(new TextOverlayPacket.Data("BEGIN!", 2000)));
            if (gameMode == GameMode.TIMER){
                timer = new ExtendableTimer(60, ()->{
                    int progress = 0;

                    getTcpServer().broadcastPacket(new ProgressBarPacket(new ProgressBarPacket.Data("", progress, Color.GREEN.getRGB(),false)));

                    var player = getServerPlayers().getFirst();

                    var matchEndingPacket = new TextOverlayPacket(new TextOverlayPacket.Data("Game Over! your score is " + player.getScore(), 5000));
                    getTcpServer().broadcastPacket(matchEndingPacket);
                    endGame();
                },
                        (remainingTime->{
                            int progress = (int) ((remainingTime / 60.0) * 100);
                            int color = getProgressBasedColor(progress);
                            getTcpServer().broadcastPacket(new ProgressBarPacket(new ProgressBarPacket.Data("Time Remaining: " + remainingTime + "s", progress, color,true)));
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
        getTcpServer().broadcastPacket(new PositionChangePacket(new PositionChangePacket.Data(serverPlayer.getPlayerId(), serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getColor())));
    }

    public void onVisibilityChange(Trap trap) {
        TrapPacket trapPacket;
        if (trap.isVisible()){
            trapPacket = new TrapPacket(new TrapPacket.Data(trap.getTrapUUID().toString(), trap.getPosX(), trap.getPosY(), Color.ORANGE.getRGB(), false));
        }else {
            trapPacket = new TrapPacket(new TrapPacket.Data(trap.getTrapUUID().toString(), trap.getPosX(), trap.getPosY(), Color.ORANGE.getRGB(), true));
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


    public int getProgressBasedColor(int progress){
        if (progress > 40) {
            return java.awt.Color.GREEN.getRGB();
        } else if (progress > 20) {
            return java.awt.Color.YELLOW.getRGB();
        } else {
            return java.awt.Color.RED.getRGB();
        }
    }

}
