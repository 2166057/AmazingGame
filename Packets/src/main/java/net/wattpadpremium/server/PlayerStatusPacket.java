package net.wattpadpremium.server;

import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerStatusPacket extends Packet<PlayerStatusPacket.Data> {

    public PlayerStatusPacket(DataInputStream inputStream) throws IOException {
        super(inputStream);
    }

    public PlayerStatusPacket(Data data) {
        super(data);
    }

    @Override
    public Data readData(DataInputStream input) throws IOException {
        long playerId = input.readLong();
        int ordinal = input.readInt();

        STATUS status = STATUS.values()[ordinal];
        boolean enabled = input.readBoolean();

        return new Data(playerId, status, enabled);
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeLong(getData().playerId());
        output.writeInt(getData().status().ordinal());
        output.writeBoolean(getData().enabled());
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.ServerPlayerStatusPacket;
    }

    public record Data(long playerId, STATUS status, boolean enabled) {}

    public enum STATUS {
        GHOSTING,
        DIZZY,
        FROZEN,
        INVISIBLE,
        BLINDED
    }
}