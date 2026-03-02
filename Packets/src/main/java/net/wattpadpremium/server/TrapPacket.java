package net.wattpadpremium.server;

import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TrapPacket extends Packet<TrapPacket.Data> {

    public TrapPacket(DataInputStream inputStream) throws IOException {
        super(inputStream);
    }

    public TrapPacket(Data data) {
        super(data);
    }

    @Override
    public Data readData(DataInputStream input) throws IOException {
        return new Data(
                input.readUTF(),
                input.readInt(),
                input.readInt(),
                input.readInt(),
                input.readBoolean()
        );
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeUTF(getData().trapID());
        output.writeInt(getData().posX());
        output.writeInt(getData().posY());
        output.writeInt(getData().color());
        output.writeBoolean(getData().delete());
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.ServerTrapPacket;
    }

    public record Data(
            String trapID,
            int posX,
            int posY,
            int color,
            boolean delete
    ) {}
}