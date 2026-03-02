package net.wattpadpremium.client;

import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AuthSessionPacket extends Packet<AuthSessionPacket.Data> {


    public AuthSessionPacket(DataInputStream inputStream) throws IOException {
        super(inputStream);
    }

    public AuthSessionPacket(Data data) {
        super(data);
    }

    @Override
    public Data readData(DataInputStream input) throws IOException {
        return new Data(input.readUTF(), input.readUTF());
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeUTF(getData().sessionToken);
        output.writeUTF(getData().username);
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.ClientAuthSessionPacket;
    }

    public record Data(String sessionToken, String username){}

}
