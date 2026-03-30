package random_generator;

import java.net.ServerSocket;
import java.net.Socket;

public class SRGServer {
    public static void main(String[] args) {
        int port = 5000;
        RandomBuffer buffer = new RandomBuffer();

        // Start the Producer thread
        new Producer(buffer).start();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("SRG Server started at port " + port);
            
            while (true) {
                // Accept new clients and start handler
                Socket client = serverSocket.accept();
                new SRGHandler(client, buffer).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}