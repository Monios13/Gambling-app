package worker;

import shared.Message;
import shared.MessageSource;
import shared.MessageType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RandomProducer implements Runnable {

    private final RandomBuffer buffer;
    private final String host;
    private final int port;
    private final String hashKey;

    public RandomProducer(RandomBuffer rb, String srgHost, int srgPort, String hashKey) {
        this.buffer = rb;
        this.host = srgHost;
        this.port = srgPort;
        this.hashKey = hashKey;
    }

    @Override
    public void run() {
        while (true) {
            try (Socket srgSocket = new Socket(host, port);
                 ObjectOutputStream out = new ObjectOutputStream(srgSocket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(srgSocket.getInputStream())) {

                out.writeObject(new Message(MessageType.RANDOM_REQUEST, MessageSource.WORKER, hashKey));
                out.flush();

                Message response = (Message) in.readObject();
                if (response.getType() == MessageType.RANDOM_RESPONSE) {
                    String payload = String.valueOf(response.getPayload());
                    String[] parts = payload.split(":");
                    if (parts.length != 2) {
                        throw new IllegalStateException("Invalid SRG payload: " + payload);
                    }

                    int number = Integer.parseInt(parts[0]);
                    String hashFromServer = parts[1];
                    String localHash = HashUtil.sha256(number + hashKey);

                    if (localHash.equals(hashFromServer)) {
                        buffer.put(number);
                    } else {
                        System.out.println("Security Alert: Invalid hash from SRG!");
                    }
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
