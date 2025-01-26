package net.wattpadpremium;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Packet {

    int getId();
    void readData(DataInputStream input) throws IOException;
    void writeData(DataOutputStream output) throws IOException;

    static Packet createPacket(int packetId, DataInputStream payload) throws IOException {
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
            case 6:
                packet = new EndGamePacket();
                packet.readData(payload);
                return packet;
            case 7:
                packet = new PlayerCountPacket();
                packet.readData(payload);
                return packet;
            case 8:
                packet = new RemovePlayerPacket();
                packet.readData(payload);
                return packet;
            default:
                return null;
        }
    }

}

