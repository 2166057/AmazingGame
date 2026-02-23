package net.wattpadpremium.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.wattpadpremium.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProgressBarPacket implements Packet {

    public static final int ID = 16;

    private String text;
    private int progress;
    private int color;
    private boolean visible;

    @Override
    public int getPacketId() {
        return 16;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        text = input.readUTF();
        progress = input.readInt();
        color = input.readInt();
        visible = input.readBoolean();
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeUTF(text);
        output.writeInt(progress);
        output.writeInt(color);
        output.writeBoolean(visible);
    }
}
