package net.wattpadpremium;

import lombok.Data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Data
public class PositionChangePacket implements Packet {

    public static final int ID = 4;

    private String username;
    private int x = 0, y = 0;
    private int color = 0;

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        username = input.readUTF(); // Read the username as a UTF string
        x = input.readInt();        // Read the X-coordinate
        y = input.readInt();
        color = input.readInt();// Read the Y-coordinate
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeUTF(username); // Write the username as a UTF string
        output.writeInt(x);        // Write the X-coordinate
        output.writeInt(y);        // Write the Y-coordinate
        output.writeInt(color);
    }
}
