package net.wattpadpremium.client;

import lombok.Data;
import net.wattpadpremium.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Data
public class MovePacket implements Packet {

    public static final int ID = 14;

    private int x = 0, y = 0;

    @Override
    public int getPacketId() {
        return ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        x = input.readInt();
        y = input.readInt();
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeInt(x);
        output.writeInt(y);
    }
}
