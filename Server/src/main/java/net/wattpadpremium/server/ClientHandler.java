package net.wattpadpremium.server;

import lombok.Getter;
import lombok.Setter;
import net.wattpadpremium.Packet;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements IClientHandler, Runnable {

    private final Socket clientSocket;
    private final TCPServer tcpServer;

    @Setter
    @Getter
    private ServerPlayer serverPlayer;

    public ClientHandler(Socket socket, TCPServer tcpServer) {
        this.clientSocket = socket;
        this.tcpServer = tcpServer;
    }

    public void run() {
        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            while (true) {
                try {
                    var packet = tcpServer.getServerPacketHandler().readPacket(in);
                    if (packet != null && tcpServer.getServerPacketHandler().containsKey(packet.getPacketId())) {
                        tcpServer.receivePacket(packet, this);
                    } else {
                        System.err.println("Unknown packet ID: " + packet.getPacketId());
                    }
                } catch (IOException e) {
                    System.err.println("Error reading packet: " + e.getMessage());
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Client connection error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            tcpServer.getClientHandlers().remove(this);
            if (getServerPlayer() != null){
                getServerPlayer().onDisconnect();
            }
            System.out.println("Client disconnected: " + clientSocket.getInetAddress());
        }
    }

    @Override
    public void sendPacketToClient(Packet packet) {
        try {
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            out.writeInt(packet.getPacketId());
            packet.writeData(out);
//                System.out.println("Sending Packet: " + packet);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending packet: " + e.getMessage());
        }
    }

}