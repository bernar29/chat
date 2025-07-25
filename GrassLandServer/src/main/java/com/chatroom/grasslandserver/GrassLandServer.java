/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.chatroom.grasslandserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author berna
 */ 
public class GrassLandServer {
    private static final int PORT = 8000;
    private static final List<PrintWriter> clientWriters = new ArrayList<>();   // To broadcast to all clients
    private static final List<String> messageHistory = new ArrayList<>();       // To store all messages
    
    public static void main(String[] args) {
        System.out.println("Chat server started on port " + PORT);
        // Start the server
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            
            while(true) {
                Socket clientSocket = serverSocket.accept();    // Waiting on clients to join
                
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket).start();        // Thread per client
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static class ClientHandler extends Thread {
        private final Socket socket;
        private PrintWriter out;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                
                synchronized (clientWriters) {
                    clientWriters.add(out);     // Add to broadcast list
                }
                
                // Send msg history to new client
                synchronized (messageHistory) {
                    for (String pastMessage : messageHistory) {
                        out.println(pastMessage);
                    }
                }
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(new Date() + "\tReceived from " + message);       // Display on server console
                    broadcast(message);                             // Send to all clients
                }
            }
            catch (IOException e) { e.printStackTrace(); }
            finally {
                synchronized (clientWriters) {
                    clientWriters.remove(out);
            }
                try {
                    socket.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        private void broadcast(String message) {
            synchronized (messageHistory) {
                messageHistory.add(message);        // Add to history
            }
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}
