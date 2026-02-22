package net.wattpadpremium;

import net.wattpadpremium.listeners.PacketListener;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

public class PacketHandler extends HashMap<Integer, PacketListener> {

    public Packet readPacket(DataInputStream input) throws IOException {
        int packetId = input.readInt();
        return Packet.createPacket(packetId, input);
    }

}
