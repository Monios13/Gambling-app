package master;

import shared.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * MasterServer — multithreaded TCP server that:
 *  - Accepts connections from the Manager console, dummy clients, and (in Phase B) the Android app.
 *  - Routes game-management commands to the correct Worker using H(GameName) mod N.
 *  - Orchestrates MapReduce search queries across all Workers via the Reducer.
 *  - Forwards play requests to the appropriate Worker.
 */
public class MasterServer {

    // ── configuration ────────────────────────────────────────────────────────
    private final int port;
    private final List<WorkerInfo> workers;   // loaded from config at startup
    private final String reducerHost;
    private final int    reducerPort;

    // ── state ─────────────────────────────────────────────────────────────────
    private volatile boolean running = true;

    // ── constructor ───────────────────────────────────────────────────────────
    public MasterServer(int port, List<WorkerInfo> workers,
                        String reducerHost, int reducerPort) {
        this.port        = port;
        this.workers     = Collections.unmodifiableList(new ArrayList<>(workers));
        this.reducerHost = reducerHost;
        this.reducerPort = reducerPort;
    }

    // ── main loop ─────────────────────────────────────────────────────────────
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

    // ── entry point ───────────────────────────────────────────────────────────
    /**
     * Usage:
     *   java master.MasterServer <masterPort> <reducerHost> <reducerPort> <configFile>
     *
     * Config file format (one worker per line):
     *   <id> <host> <port>
     *   e.g.
     *   0 localhost 6001
     *   1 localhost 6002
     */
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