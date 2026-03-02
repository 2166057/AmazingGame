package net.wattpadpremium.server;

import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketType;

import javax.lang.model.type.NullType;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EndGamePacket extends Packet<Void> {


    public EndGamePacket(DataInputStream inputStream) throws IOException {
        super(inputStream);
    }

    public EndGamePacket() {
        super((Void) null);
    }

    @Override
    public Void readData(DataInputStream input) throws IOException {
        return null;
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {

    }

    @Override
    public PacketType getPacketType() {
        return PacketType.ServerEndGamePacket;
    }

}
