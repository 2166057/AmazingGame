package net.wattpadpremium;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStatusPacket implements Packet{

    public static final int ID = 11;

    private String targetPlayerUsername;
    private STATUS status;
    private boolean enabled;

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        targetPlayerUsername = input.readUTF();
        status = STATUS.values()[input.readInt()];
        enabled = input.readBoolean();
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeUTF(targetPlayerUsername);
        output.writeInt(status.ordinal());
        output.writeBoolean(enabled);
    }

    public enum STATUS {
        GHOSTING, DIZZY, FROZEN, INVISIBLE, BLINDED
    }

}
