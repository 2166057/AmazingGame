package net.wattpadpremium.server;

import lombok.Getter;
import lombok.Setter;
import net.wattpadpremium.Packet;
import net.wattpadpremium.server.handler.ServerPacketHandler;

import java.io.*;
import java.net.*;
import java.util.*;

public class TCPServer {

    private static final int SERVER_PORT = 12345;
    private final ServerSocket serverSocket;
    private final List<ClientHandler> clientHandlers = new ArrayList<>();

    @Getter
    private final ServerPacketHandler serverPacketHandler;

    public TCPServer() throws IOException {
        serverSocket = new ServerSocket(SERVER_PORT);
        serverPacketHandler = new ServerPacketHandler();
    }

    public void startServer() {
        System.out.println("Server started on port " + SERVER_PORT);
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastPacket(Packet packet) {
        synchronized (clientHandlers){
            for (ClientHandler clientHandler : clientHandlers) {
                clientHandler.sendPacket(packet);
            }
        }
    }

    public static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final TCPServer tcpServer;

        @Setter
        @Getter
        private ServerPlayer serverPlayer;

        public ClientHandler(Socket socket, TCPServer tcpServer) {
            this.clientSocket = socket;
            this.tcpServer = tcpServer;
        }

        @Override
        public void run() {
            try {
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                while (true) {
                    try {
                        tcpServer.serverPacketHandler.handlePacket(in, this);
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
                tcpServer.clientHandlers.remove(this);
                if (getServerPlayer() != null){
                    getServerPlayer().onDisconnect();
                }
                System.out.println("Client disconnected: " + clientSocket.getInetAddress());
            }
        }

        public void sendPacket(Packet packet) {
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
}
