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
public class TextOverlayPacket implements Packet {

    public static final int ID = 15;

    private String text;
    private int durationMS;

    @Override
    public int getPacketId() {
        return ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {
        text = input.readUTF();
        durationMS = input.readInt();
    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {
        output.writeUTF(text);
        output.writeInt(durationMS);
    }
}
