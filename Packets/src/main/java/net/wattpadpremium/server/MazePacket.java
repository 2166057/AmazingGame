package net.wattpadpremium.server;

import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MazePacket extends Packet<MazePacket.Data> {

    public MazePacket(DataInputStream input) throws IOException {
        super(input);
    }

    public MazePacket(Data data) {
        super(data);
    }

    @Override
    protected Data readData(DataInputStream input) throws IOException {
        int width = input.readInt();
        int height = input.readInt();

        int[][] maze = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                maze[y][x] = input.readInt();
            }
        }

        int goalX = input.readInt();
        int goalY = input.readInt();

        return new Data(maze, goalX, goalY);
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        Data data = getData();
        int[][] maze = data.maze();

        if (maze == null || maze.length == 0 || maze[0].length == 0) {
            throw new IOException("Maze dimensions must be valid.");
        }

        int width = maze[0].length;
        int height = maze.length;

        output.writeInt(width);
        output.writeInt(height);

        for (int[] row : maze) {
            for (int cell : row) {
                output.writeInt(cell);
            }
        }

        output.writeInt(data.goalX());
        output.writeInt(data.goalY());
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.ServerMazePacket;
    }

    public record Data(int[][] maze, int goalX, int goalY) {}
}