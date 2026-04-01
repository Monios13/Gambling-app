package random_generator;
import shared.Message;
import shared.MessageSource;
import shared.MessageType;
import java.io.*;
import java.net.*;

public class SRGTest {
    public static void main(String[] args) throws Exception {
        try (Socket s = new Socket("localhost", 5000);
             ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
            
            System.out.println("Connecting to SRG on port 5000...");
            out.writeObject(new Message(MessageType.RANDOM_REQUEST, MessageSource.WORKER, "testSecret123"));
            out.flush();

            Message response = (Message) in.readObject();
            System.out.println("Response from SRG: " + response.getPayload());
        }
    }
}
