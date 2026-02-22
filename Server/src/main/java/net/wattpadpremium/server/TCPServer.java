package net.wattpadpremium.server;

import lombok.Getter;
import lombok.Setter;
import net.wattpadpremium.Packet;
import net.wattpadpremium.server.handler.ServerPacketHandler;

import java.io.*;
import java.net.*;
import java.util.*;

public class TCPServer extends AbstractTCPServer{

    private final int serverPort;
    private final ServerSocket serverSocket;

    @Getter
    private final List<ClientHandler> clientHandlers = new ArrayList<>();

    @Getter
    private final ServerPacketHandler serverPacketHandler;

    public TCPServer(int port) throws IOException {
        this.serverPort = port;
        serverSocket = new ServerSocket(serverPort);
        serverPacketHandler = new ServerPacketHandler();
    }

    public void startServer() {
        System.out.println("Server started on port " + serverPort);
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                getClientHandlers().add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
