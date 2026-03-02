package net.wattpadpremium.server;

import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerCountPacket extends Packet<PlayerCountPacket.Data> {

    public PlayerCountPacket(DataInputStream inputStream) throws IOException {
        super(inputStream);
    }

    public PlayerCountPacket(Data data) {
        super(data);
    }

    @Override
    public Data readData(DataInputStream input) throws IOException {
        return new Data(
                input.readInt(),
                input.readInt()
        );
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeInt(getData().count());
        output.writeInt(getData().max());
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.ServerPlayerCountPacket;
    }

    public record Data(int count, int max) {}
}