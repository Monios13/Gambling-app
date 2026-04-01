package random_generator;

import shared.Message;
import shared.MessageSource;
import shared.MessageType;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;

public class SRGHandler extends Thread {
    
    private Socket client;
    private RandomBuffer buffer;

    public SRGHandler(Socket client, RandomBuffer buffer) {
        this.client = client;
        this.buffer = buffer;
    }

    
    private String generateSecureResponse(String secret) {
        try {
            int number = buffer.get(); // Get from synchronized buffer
            
            // SHA-256 logic
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((number + secret).getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String hash = sb.toString();
            
            return number + ":" + hash;
        } catch (Exception e) {
            return "ERROR";
        }
    }

    @Override
    public void run() {
        try {
            // Using ObjectStreams as required for Message class
            ObjectInputStream in = new ObjectInputStream(client.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());

            // Read the Message object
            Object input = in.readObject();
            
            if (input instanceof Message) {
                Message msg = (Message) input;
                if (msg.getType() == MessageType.RANDOM_REQUEST) {
                    String secret = (String) msg.getPayload();
                    
                    // Processing and response
                    String result = generateSecureResponse(secret);
                    out.writeObject(new Message(MessageType.RANDOM_RESPONSE, MessageSource.SRG, result));
                    out.flush();
                }
            }
            
            client.close(); 
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
