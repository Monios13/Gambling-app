import shared.Message;
import shared.MessageType;
import shared.MessageSource;
import java.io.*;
import java.net.*;

public class SRGTest {
    public static void main(String[] args) {
        String[] keys = {"Srg_Safe_v1_!Lo", "Hi_Stakes_999@!_SRG"};

        for (String key : keys) {
            new Thread(() -> {
                try (Socket socket = new Socket("localhost", 5000);
                     ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                     ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                    // Στέλνουμε το αίτημα
                    Message request = new Message(MessageType.RANDOM_REQUEST, MessageSource.WORKER, key);
                    out.writeObject(request);
                    out.flush();

                    // Παίρνουμε τον αριθμό
                    int randomNumber = in.readInt();
                    System.out.println("[TEST] Received from SRG for key " + key + ": " + randomNumber);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}