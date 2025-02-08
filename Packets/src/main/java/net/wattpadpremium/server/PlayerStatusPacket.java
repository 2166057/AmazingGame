package net.wattpadpremium.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.wattpadpremium.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStatusPacket implements Packet {

    public static final int ID = 11;

    private long playerId;
    private STATUS status;
    private boolean enabled;

    @Override
    public int getPacketId() {
        return ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        playerId = input.readLong();
        status = STATUS.values()[input.readInt()];
        enabled = input.readBoolean();
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeLong(playerId);
        output.writeInt(status.ordinal());
        output.writeBoolean(enabled);
    }

    public enum STATUS {
        GHOSTING, DIZZY, FROZEN, INVISIBLE, BLINDED
    }

}
