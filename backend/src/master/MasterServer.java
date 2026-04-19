package master;

import shared.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class MasterServer {

    
    private final int port;
    private final List<WorkerInfo> workers;   
    private final String reducerHost;
    private final int    reducerPort;

    
    private volatile boolean running = true;

    public MasterServer(int port, List<WorkerInfo> workers,
                        String reducerHost, int reducerPort) {
        this.port        = port;
        this.workers     = Collections.unmodifiableList(new ArrayList<>(workers));
        this.reducerHost = reducerHost;
        this.reducerPort = reducerPort;
    }

    //  main loop 
    public void start() {
        System.out.println("[Master] Starting on port " + port
                + " | workers=" + workers.size()
                + " | reducer=" + reducerHost + ":" + reducerPort);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    // Each client is served in its own thread
                    Thread t = new Thread(
                            new ClientHandler(clientSocket, workers, reducerHost, reducerPort),
                            "client-" + clientSocket.getRemoteSocketAddress());
                    t.setDaemon(true);
                    t.start();
                } catch (IOException e) {
                    if (running) {
                        System.err.println("[Master] Accept error: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[Master] Cannot open server socket on port " + port + ": " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
    }

   
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: MasterServer <masterPort> <reducerHost> <reducerPort> <configFile>");
            System.exit(1);
        }

        int    masterPort   = Integer.parseInt(args[0]);
        String reducerHost  = args[1];
        int    reducerPort  = Integer.parseInt(args[2]);
        String configFile   = args[3];

        List<WorkerInfo> workers = WorkerConfigLoader.load(configFile);

        MasterServer server = new MasterServer(masterPort, workers, reducerHost, reducerPort);
        server.start();
    }
}