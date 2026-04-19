package reducer;

import java.io.*;
import java.net.*;

public class ReducerServer {

    private final int port;
    private volatile boolean running = true;

    private final ReduceAggregator aggregator = new ReduceAggregator();

    public ReducerServer(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("[Reducer] Starting on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);

            while (running) {
                try {
                    Socket client = serverSocket.accept();
                    Thread t = new Thread(
                            new ReducerHandler(client, aggregator),
                            "reducer-handler-" + client.getRemoteSocketAddress());
                    t.setDaemon(true);
                    t.start();
                } catch (IOException e) {
                    if (running) System.err.println("[Reducer] Accept error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[Reducer] Cannot open socket on port " + port + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: ReducerServer <port>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        new ReducerServer(port).start();
    }
}