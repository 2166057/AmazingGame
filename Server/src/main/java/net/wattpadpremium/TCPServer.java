import java.io.*;
import java.net.*;
import java.util.*;

public class TCPServer {
    private static final int SERVER_PORT = 12345;
    private ServerSocket serverSocket;
    private List<ClientHandler> clientHandlers;

    public TCPServer() throws IOException {
        serverSocket = new ServerSocket(SERVER_PORT);
        clientHandlers = new ArrayList<>();
    }

    // Method to handle client communication
    public void startServer() {
        try {
            System.out.println("Server started, waiting for clients...");
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accept a new client connection
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Create a new ClientHandler thread for each client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcast message to all connected clients
    public void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessage(message);
        }
    }

    public static void main(String[] args) {
        try {
            TCPServer server = new TCPServer();
            server.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inner class to handle communication with each client
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;
        
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                // Set up input and output streams for the client
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    System.out.println("Received from client: " + clientMessage);

                    // Echo the received message back to the client
                    out.println("Server received: " + clientMessage);

                    // Broadcast the message to all connected clients
                    broadcastMessageToAll("Client says: " + clientMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Send a message to this specific client
        public void sendMessage(String message) {
            out.println(message);
        }

        // Broadcast message to all clients
        public static void broadcastMessageToAll(String message) {
            // Send message to all clients
            for (ClientHandler handler : clientHandlers) {
                handler.sendMessage(message);
            }
        }
    }
}
