package net.wattpadpremium.handler;

import net.wattpadpremium.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

public class ServerPacketHandler extends HashMap<Integer, ServerPacketListener> {

    public void handlePacket(DataInputStream input, TCPServer.ClientHandler clientHandler) throws IOException {
        int packetId = input.readInt();

        Packet packet = Packet.createPacket(packetId, input);
        if (packet != null && containsKey(packetId)) {
            get(packetId).handlePacket(packet, clientHandler);
        } else {
            System.err.println("Unknown packet ID: " + packetId);
        }
    }



}
