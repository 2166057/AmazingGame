package net.wattpadpremium.server;

import lombok.Data;
import net.wattpadpremium.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Data
public class AcceptConnectionPacket implements Packet {

    public static final int ID = 13;

    private String username;
    private long playerId;

    @Override
    public int getPacketId() {
        return ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        username = input.readUTF();
        playerId = input.readLong();
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeUTF(username);
        output.writeLong(playerId);
    }
}
