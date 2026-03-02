package net.wattpadpremium;

import lombok.Getter;

import javax.lang.model.type.NullType;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


@Getter
public class KeepAlivePacket extends Packet<NullType> {

    protected KeepAlivePacket(DataInputStream inputStream) throws IOException {
        super(inputStream);
    }

    protected KeepAlivePacket(NullType data) {
        super(data);
    }

    @Override
    protected NullType readData(DataInputStream input) throws IOException {
        return null;
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {

    }

    @Override
    public PacketType getPacketType() {
        return PacketType.GlobalKeepAlivePacket;
    }


}
