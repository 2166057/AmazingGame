package net.wattpadpremium.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.wattpadpremium.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Data
@AllArgsConstructor
public class PlayerScorePacket implements Packet {

    public static final int ID = 5;

    public PlayerScorePacket(){}

    private long playerId;
    private int score;

    @Override
    public int getPacketId() {
        return ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        this.playerId = input.readLong();
        this.score = input.readInt();
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeLong(playerId);
        output.writeInt(score);
    }
}
