package net.wattpadpremium.server;

import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RemovePlayerPacket extends Packet<RemovePlayerPacket.Data> {

    public RemovePlayerPacket(DataInputStream inputStream) throws IOException {
        super(inputStream);
    }

    public RemovePlayerPacket(Data data) {
        super(data);
    }

    @Override
    public Data readData(DataInputStream input) throws IOException {
        return new Data(input.readLong());
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeLong(getData().playerId());
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.ServerRemovePlayerPacket;
    }

    public record Data(long playerId) {}
}