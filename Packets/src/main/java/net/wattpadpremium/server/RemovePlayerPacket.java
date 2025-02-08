package net.wattpadpremium.server;

import lombok.Data;
import lombok.Getter;
import net.wattpadpremium.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Getter
@Data
public class RemovePlayerPacket implements Packet {

    public RemovePlayerPacket(){}

    public static final int ID = 8;
    private long playedId;

    public RemovePlayerPacket(Long playedId) {
        this.playedId = playedId;
    }

    @Override
    public int getPacketId() {
        return RemovePlayerPacket.ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        playedId = input.readLong();
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeLong(playedId);
    }
}
