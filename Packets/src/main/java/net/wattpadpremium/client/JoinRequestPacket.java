package net.wattpadpremium.client;

import lombok.Getter;
import lombok.Setter;
import net.wattpadpremium.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Setter
@Getter
public class JoinRequestPacket implements Packet {

    public static final int ID = 2;

    private int color;

    @Override
    public int getPacketId() {
        return ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        this.color = input.readInt();
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeInt(color);
    }
}
