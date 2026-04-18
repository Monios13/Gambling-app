package random_generator;

import java.io.*;
import java.net.*;
import shared.Message;
import shared.MessageSource;
import shared.MessageType;
import worker.HashUtil; 

public class SRGServer {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Secure Random Generator Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Πολυνηματικότητα: Κάθε αίτημα σε νέο Thread
                new Thread(new ClientTask(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientTask implements Runnable {
        private final Socket socket;

        public ClientTask(Socket socket) { this.socket = socket; }

        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

                // Λήψη του Message από τον Worker
                Message request = (Message) in.readObject();
                String hashKey = (String) request.getPayload();

                // Λήψη αριθμού από τον Buffer του παιχνιδιού
                int result = SRGHandler.getNextNumber(hashKey);

                // Επιστροφή αποτελέσματος
                String hash = HashUtil.sha256(result + hashKey);
                String payload = result + ":" + hash;
                out.writeObject(new Message(MessageType.RANDOM_RESPONSE, MessageSource.SRG, payload));
                out.flush();
                
                System.out.println("[SRG] Sent number " + result + " for key: " + hashKey);

            } catch (Exception e) {
                System.err.println("Error handling SRG request: " + e.getMessage());
            } finally {
                try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }
}