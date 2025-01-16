package net.wattpadpremium;

import lombok.Getter;
import lombok.Setter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Setter
@Getter
public class JoinPacket implements Packet {

    public static final int ID = 2;

    private String username;
    private int color;

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        this.username = input.readUTF();
        this.color = input.readInt();
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeUTF(username);
        output.writeInt(color);
    }
}
