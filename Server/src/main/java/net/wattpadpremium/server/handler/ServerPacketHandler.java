package net.wattpadpremium.server.handler;

import net.wattpadpremium.*;
import net.wattpadpremium.server.IClientHandler;
import net.wattpadpremium.server.TCPServer;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

public class ServerPacketHandler extends HashMap<Integer, ServerPacketListener> {

    public Packet readPacket(DataInputStream input) throws IOException {
        int packetId = input.readInt();
        return Packet.createPacket(packetId, input);
    }

}
