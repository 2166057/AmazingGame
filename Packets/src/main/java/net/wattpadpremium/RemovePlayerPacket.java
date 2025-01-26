package net.wattpadpremium;

import lombok.Data;
import lombok.Getter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Getter
@Data
public class RemovePlayerPacket implements Packet{

    public RemovePlayerPacket(){}

    public static final int ID = 8;
    private String username;

    public RemovePlayerPacket(String username) {
        this.username = username;
    }

    @Override
    public int getId() {
        return RemovePlayerPacket.ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        username = input.readUTF();
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeUTF(username);
    }
}
