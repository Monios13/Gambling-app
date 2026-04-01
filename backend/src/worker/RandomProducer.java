package worker;

import java.net.Socket;
import java.io.*;
import shared.Message;
import shared.MessageSource;
import shared.MessageType;

public class RandomProducer implements Runnable {

    private RandomBuffer buffer;
    private String host;
    private int port;
    private String hashKey;

    public RandomProducer(RandomBuffer rb, String srgHost, int srgPort, String hashKey) {
        this.buffer = rb;
        this.host = srgHost;
        this.port = srgPort;
        this.hashKey = hashKey;
    }

    @Override
    public void run() {
        while (true) {
            try(Socket srgSocket = new Socket(host, port); // TCP επικοινωνία [cite: 107]
                ObjectOutputStream out = new ObjectOutputStream(srgSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(srgSocket.getInputStream())) {

                out.writeObject(new Message(MessageType.RANDOM_REQUEST, MessageSource.WORKER, hashKey));
                out.flush();

                Message response = (Message) in.readObject();
                if (response.getType() == MessageType.RANDOM_RESPONSE) {
                    String payload = (String) response.getPayload();
                    String[] parts = payload.split(":");
                    int number = Integer.parseInt(parts[0]);
                    String hashFromServer = parts[1];

                    String localHash = HashUtil.sha256(number + hashKey);

                    if (localHash.equals(hashFromServer)){
                        buffer.put(number);
                    } else {
                        System.out.println("Security Alert: Invalid hash from SRG!");
                    }
                }
                buffer.put((Integer) response.getPayload());
            } catch (Exception e) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException i) {}
            }
        }
    }
}
