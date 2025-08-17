package com.chatroom.grasslandforum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * Grassland is a secure method for contacting your group, it does not contain 
 * much overhead besides few features and very strong encryption (AES) - Not available.
 * It's meant to be as lightweight, fast, and as safe as possible. But you 
 * should implement best security practices to mitigate information analysis
 * tools.
 */
public class App extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8000;    
    
    private TextArea chatArea;          // Displays the messages
    private TextField messageField;     // Input field for messages
    private PrintWriter out;
    private String clientName;

    @Override
    public void start(Stage primaryStage) {
        // Prompt for client name (could be improved)
        System.out.print("Enter your alias: ");
        clientName = new Scanner(System.in).nextLine();     // Temporary input
    
        // UI setup
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        
        messageField = new TextField();
        messageField.setPromptText("Type your message...");
        messageField.setOnAction(e -> sendMessage());
        
        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendMessage());
        
        HBox inputBox = new HBox(10, messageField, sendButton);
        
        VBox root = new VBox(10, chatArea, inputBox);
        
        root.setPadding(new Insets(10));
        
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Chat Client - " + clientName);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Connect to the server and start listener thread
        connectToServer();
    }
    
    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            appendMessage("Connected to server. Loading chat history...");
            
            // Thread to listen for incoming messages (history and new)
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message;
                    
                    while((message = in.readLine()) != null) {
                        String finalMessage = message;
                        Platform.runLater(() -> appendMessage(finalMessage));   //update UI in FX thread
                    }
                }
                catch (IOException e) {
                    Platform.runLater(() -> appendMessage("Connection lost."));
                }
                
            }).start();
        }
        catch (IOException e) {
            appendMessage("Failed to connect: " + e.getMessage());
        }
    }
    
    private void sendMessage() {
        String message = messageField.getText().trim();
        if(!message.isEmpty()) {
            String fullMessage = clientName + ": " + message;
            out.println(fullMessage);
            messageField.clear();
        }
    }
    
    private void appendMessage(String message) {
        chatArea.appendText(message + "\n");
    }

    public static void main(String[] args) {
        launch();
    }


}
