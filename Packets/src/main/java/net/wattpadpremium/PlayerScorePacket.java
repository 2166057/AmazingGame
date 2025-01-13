package net.wattpadpremium;

import lombok.Data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Data
public class PlayerScorePacket implements Packet {


    public static final int ID = 5;

    public PlayerScorePacket(){

    }

    public PlayerScorePacket(String username, int score) {
        this.username = username;
        this.score = score;
    }


    private String username;
    private int score;

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        this.username = input.readUTF();
        this.score = input.readInt();
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeUTF(username);
        output.writeInt(score);
    }
}
