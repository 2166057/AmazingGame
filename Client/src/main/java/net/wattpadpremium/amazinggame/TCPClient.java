package net.wattpadpremium.amazinggame;

import java.io.*;
import java.net.*;

public class TCPClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public void startClient() throws IOException {
        Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Create a BufferedReader to read user input from the console
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Client connected to server. Type your messages:");

        // Send and receive messages
        String message;
        while ((message = userInput.readLine()) != null) {
            // Send message to the server
            out.println(message);

            // Receive server's response
            String serverResponse = in.readLine();
            System.out.println("Server says: " + serverResponse);
        }

        socket.close();
    }

    public static void main(String[] args) {
        try {
            TCPClient client = new TCPClient();
            client.startClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
