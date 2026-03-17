package random_generator;

import shared.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Secure Random Generator TCP Server.
 * Για απλότητα στο Παραδοτέο Α, ο Worker στέλνει το secret S,
 * ο SRG επιστρέφει (number, sha256(number+secret)).
 */
public class SRGServer {

    private final int port;
    private final SecureRandom secureRandom = new SecureRandom();

    public SRGServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("SRG listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                Thread t = new Thread(new SRGClientHandler(socket, secureRandom));
                t.start();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java random_generator.SRGServer <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        SRGServer server = new SRGServer(port);
        server.start();
    }
}

class SRGClientHandler implements Runnable {

    private final Socket socket;
    private final SecureRandom secureRandom;

    SRGClientHandler(Socket socket, SecureRandom secureRandom) {
        this.socket = socket;
        this.secureRandom = secureRandom;
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            while (true) {
                Object obj = in.readObject();
                if (!(obj instanceof Message)) {
                    continue;
                }
                Message msg = (Message) obj;
                if (!"RANDOM_REQUEST".equals(msg.getType())) {
                    out.writeObject(new Message("ERROR", "Unsupported SRG message type"));
                    out.flush();
                    continue;
                }
                if (!(msg.getPayload() instanceof String)) {
                    out.writeObject(new Message("ERROR", "Secret expected as String payload"));
                    out.flush();
                    continue;
                }
                String secret = (String) msg.getPayload();
                int number = secureRandom.nextInt(Integer.MAX_VALUE);
                String hash = sha256(number + secret);
                out.writeObject(new Message("RANDOM_RESPONSE", number + ":" + hash));
                out.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("SRG client handler error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}

