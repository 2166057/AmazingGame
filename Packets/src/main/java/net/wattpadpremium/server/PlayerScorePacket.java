package net.wattpadpremium.server;

import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerScorePacket extends Packet<PlayerScorePacket.Data> {

    public PlayerScorePacket(DataInputStream inputStream) throws IOException {
        super(inputStream);
    }

    public PlayerScorePacket(Data data) {
        super(data);
    }

    @Override
    public Data readData(DataInputStream input) throws IOException {
        return new Data(
                input.readLong(),
                input.readInt()
        );
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeLong(getData().playerId());
        output.writeInt(getData().score());
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.ServerPlayerScorePacket;
    }

    public record Data(long playerId, int score) {}
}