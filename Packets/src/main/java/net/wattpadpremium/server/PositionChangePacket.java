package net.wattpadpremium.server;

import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PositionChangePacket extends Packet<PositionChangePacket.Data> {


    public PositionChangePacket(DataInputStream inputStream) throws IOException {
        super(inputStream);
    }

    public PositionChangePacket(Data data) {
        super(data);
    }

    @Override
    protected Data readData(DataInputStream input) throws IOException {
        long playerId = input.readLong();
        int x = input.readInt();
        int y = input.readInt();
        int color = input.readInt();

        return new Data(playerId, x, y, color);
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        Data data = getData();

        output.writeLong(data.playerId());
        output.writeInt(data.x());
        output.writeInt(data.y());
        output.writeInt(data.color());
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.ServerPositionChangePacket;
    }

    public record Data(long playerId, int x, int y, int color) {}
}