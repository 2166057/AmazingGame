package net.wattpadpremium.amazinggame.client;

import lombok.Getter;
import net.wattpadpremium.Packet;
import net.wattpadpremium.PacketHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public class TCPClient {

    private final Socket socket;
    @Getter
    private final PacketHandler packetHandler;


    public TCPClient(String serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        this.packetHandler = new PacketHandler();
        new Thread(this::listenForPackets).start();
    }

    public void sendPacket(Packet packet) {
        CompletableFuture.runAsync(()->{
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeInt(packet.getId());
                packet.writeData(out);
//                System.out.println("Sending Packet: " + packet);
                out.flush();
            } catch (IOException e) {
                System.err.println("Error sending packet: " + e.getMessage());
            }
        });
    }

    public void listenForPackets() {
        try {
            while (true) {
                try {
                    packetHandler.handlePacket(new DataInputStream(socket.getInputStream()));
                } catch (IOException e) {
                    System.err.println("Error reading packet: " + e.getMessage());
                    break;
                }
            }
        } finally {
            stopClient();
        }
    }

    public void stopClient() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}