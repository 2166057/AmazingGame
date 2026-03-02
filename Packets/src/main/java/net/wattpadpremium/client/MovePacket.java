package net.wattpadpremium.client;

import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MovePacket extends Packet<MovePacket.Data> {

    public MovePacket(DataInputStream input) throws IOException {
        super(input);
    }

    public MovePacket(Data data) {
        super(data);
    }

    @Override
    public MovePacket.Data readData(DataInputStream input) throws IOException {
        return new Data(input.readInt(), input.readInt());
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeInt(getData().x);
        output.writeInt(getData().y);
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.ClientMovePacket;
    }

    public record Data(int x, int y) {}
}
