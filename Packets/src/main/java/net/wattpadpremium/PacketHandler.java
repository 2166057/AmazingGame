package net.wattpadpremium;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

public class PacketHandler extends HashMap<Integer, PacketListener> {

    public void handlePacket(DataInputStream input) throws IOException {
        int packetId = input.readInt();

        Packet packet = createPacket(packetId, input);
        if (packet != null && containsKey(packetId)) {
            get(packetId).handlePacket(packet);
        } else {
            System.err.println("Unknown packet ID: " + packetId);
        }
    }

    private Packet createPacket(int packetId, DataInputStream payload) throws IOException {
        Packet packet;
        switch (packetId) {
            case 1:
                packet = new KeepAlivePacket();
                packet.readData(payload);
                return packet;
            case 2:
                packet = new JoinPacket();
                packet.readData(payload);
                return packet;
            case 3:
                packet = new MazePacket();
                packet.readData(payload);
                return packet;
            case 4:
                packet = new PositionChangePacket();
                packet.readData(payload);
                return packet;
            case 5:
                packet = new PlayerScorePacket();
                packet.readData(payload);
                return packet;
            default:
                return null;
        }
    }


}
