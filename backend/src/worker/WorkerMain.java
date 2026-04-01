package worker;

import shared.Game;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WorkerMain {

    public static final Map<String, Game> gamesStorage = Collections.synchronizedMap(new HashMap<>());
    public static final Map<String, RandomBuffer> buffers = Collections.synchronizedMap(new HashMap<>());
    public static final Map<String, Double> playerStats = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        int port = 8081;
        String srgHost = "localhost";
        int srgPort = 5000;

        if (args.length >= 3) {
            port = Integer.parseInt(args[0]);
            srgHost = args[1];
            srgPort = Integer.parseInt(args[2]);
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Worker Node started at port " + port);
            while (true) {
                Socket masterSocket = serverSocket.accept();
                new WorkerThread(masterSocket, srgHost, srgPort).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
