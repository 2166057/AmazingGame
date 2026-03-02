package net.wattpadpremium.server;

import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AcceptConnectionPacket extends Packet<AcceptConnectionPacket.Data> {


    public AcceptConnectionPacket(DataInputStream inputStream) throws IOException {
        super(inputStream);
    }

    public AcceptConnectionPacket(Data data) {
        super(data);
    }

    @Override
    public Data readData(DataInputStream input) throws IOException {
        return new Data(input.readUTF(), input.readLong());
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeUTF(getData().username);
        output.writeLong(getData().playerId);
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.ServerAcceptConnectionPacket;
    }

    public record Data(String username, Long playerId) {}

}
