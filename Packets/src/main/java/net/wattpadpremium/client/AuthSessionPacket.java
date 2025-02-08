package net.wattpadpremium.client;

import lombok.Data;
import net.wattpadpremium.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Data
public class AuthSessionPacket implements Packet {

    public static final int ID = 12;

    private String sessionToken;
    private String username = "";


    @Override
    public int getPacketId() {
        return ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        sessionToken = input.readUTF();
        username = input.readUTF();
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeUTF(sessionToken);
        output.writeUTF(username);
    }
}
