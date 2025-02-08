package net.wattpadpremium.server;

import lombok.Data;
import net.wattpadpremium.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Data
public class PositionChangePacket implements Packet {

    public static final int ID = 4;

    private long playerId;
    private int x = 0, y = 0;
    private int color = 0;

    @Override
    public int getPacketId() {
        return ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        playerId = input.readLong();
        x = input.readInt();
        y = input.readInt();
        color = input.readInt();
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeLong(playerId);
        output.writeInt(x);
        output.writeInt(y);
        output.writeInt(color);
    }
}
