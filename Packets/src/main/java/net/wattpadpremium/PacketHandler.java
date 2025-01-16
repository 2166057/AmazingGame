package net.wattpadpremium;

import net.wattpadpremium.listeners.PacketListener;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

public class PacketHandler extends HashMap<Integer, PacketListener> {

    public void handlePacket(DataInputStream input) throws IOException {
        int packetId = input.readInt();

        Packet packet = Packet.createPacket(packetId, input);
        if (packet != null && containsKey(packetId)) {
            get(packetId).handlePacket(packet);
        } else {
            System.err.println("Unknown packet ID: " + packetId);
        }
    }

}
