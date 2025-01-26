package net.wattpadpremium;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class TrapPacket implements Packet {

    public static final int ID = 9;

    private String trapID;
    private int posX, posY;
    private int color;
    private boolean delete;

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        trapID = input.readUTF();
        posX = input.readInt();
        posY = input.readInt();
        color = input.readInt();
        delete = input.readBoolean();
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeUTF(trapID);
        output.writeInt(posX);
        output.writeInt(posY);
        output.writeInt(color);
        output.writeBoolean(delete);
    }
}
