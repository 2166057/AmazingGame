package net.wattpadpremium;

import lombok.Data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Data
public class PlayerCountPacket implements Packet {

    public static final int ID = 7;

    private int count = 0;
    private int max = 0;

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        count = input.readInt();
        max = input.readInt();
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeInt(count);
        output.writeInt(max);
    }

}
