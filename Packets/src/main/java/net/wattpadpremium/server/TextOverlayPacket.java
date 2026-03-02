package net.wattpadpremium.server;

import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketType;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TextOverlayPacket extends Packet<TextOverlayPacket.Data> {

    public TextOverlayPacket(DataInputStream inputStream) throws IOException {
        super(inputStream);
    }

    public TextOverlayPacket(Data data) {
        super(data);
    }

    @Override
    public Data readData(DataInputStream input) throws IOException {
        return new Data(
                input.readUTF(),
                input.readInt()
        );
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeUTF(getData().text());
        output.writeInt(getData().durationMS());
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.ServerTextOverlayPacket;
    }

    public record Data(@NotNull String text, int durationMS) {}
}