package net.wattpadpremium.client;

import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class JoinRequestPacket extends Packet<JoinRequestPacket.Data> {


    public JoinRequestPacket(DataInputStream inputStream) throws IOException {
        super(inputStream);
    }

    public JoinRequestPacket(Data data) {
        super(data);
    }

    @Override
    public JoinRequestPacket.Data readData(DataInputStream input) throws IOException {
        return new Data(input.readInt());
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeInt(getData().color);
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.ClientJoinRequestPacket;
    }


    public record Data(int color){}
}
