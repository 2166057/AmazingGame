package net.wattpadpremium;

import lombok.Data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Data
public class MazePacket implements Packet {

    public static final int ID = 3;

    private int[][] maze;
    private int goalX, goalY;

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        int width = input.readInt();  // Read the width of the maze
        int height = input.readInt(); // Read the height of the maze

        maze = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                maze[i][j] = input.readInt();
            }
        }

        goalX = input.readInt();
        goalY = input.readInt();
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        if (maze == null) {
            throw new IOException("Maze data is null.");
        }

        int width = maze[0].length; // Number of columns
        int height = maze.length;  // Number of rows

        output.writeInt(width);
        output.writeInt(height);

        for (int[] row : maze) {
            for (int cell : row) {
                output.writeInt(cell);
            }
        }

        output.writeInt(goalX);
        output.writeInt(goalY);
    }

    public MazePacket(int[][] maze) {
        if (maze == null || maze.length == 0 || maze[0].length == 0) {
            throw new IllegalArgumentException("Maze dimensions must be valid.");
        }
        this.maze = maze;
    }

    // Default constructor
    public MazePacket() {
    }
}
