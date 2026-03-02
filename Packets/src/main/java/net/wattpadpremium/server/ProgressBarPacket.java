package net.wattpadpremium.server;

import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ProgressBarPacket extends Packet<ProgressBarPacket.Data> {

    public ProgressBarPacket(DataInputStream inputStream) throws IOException {
        super(inputStream);
    }

    public ProgressBarPacket(Data data) {
        super(data);
    }

    @Override
    public Data readData(DataInputStream input) throws IOException {
        return new Data(
                input.readUTF(),
                input.readInt(),
                input.readInt(),
                input.readBoolean()
        );
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeUTF(getData().text());
        output.writeInt(getData().progress());
        output.writeInt(getData().color());
        output.writeBoolean(getData().visible());
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.ServerProgressBarPacket;
    }

    public record Data(String text, int progress, int color, boolean visible) {}
}