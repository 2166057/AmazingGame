package net.wattpadpremium.server;

import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class MazeStylePacket extends Packet<MazeStylePacket.Data> {

    public MazeStylePacket(Data data) {
        super(data);
    }

    public MazeStylePacket(DataInputStream payload) throws IOException {
        super(payload);
    }

    @Override
    protected Data readData(DataInputStream input) throws IOException {
        return new Data(input.readInt(), input.readInt());
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeInt(getData().groundColor);
        output.writeInt(getData().wallColor);
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.ServerMazeStylePacket;
    }

    public record Data(int groundColor, int wallColor) {}

}
