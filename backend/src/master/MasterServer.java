package master;

import shared.Game;
import shared.Message;
import shared.PlayRequest;
import shared.PlayResult;
import shared.SearchRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Πολυνηματικός Master TCP Server.
 * Δέχεται συνδέσεις από manager / dummy player / μελλοντικά Android
 * και δρομολογεί τα αιτήματα προς τους Workers.
 */
public class MasterServer {

    private final int port;
    private final List<WorkerInfo> workers;

    public MasterServer(int port, List<WorkerInfo> workers) {
        this.port = port;
        this.workers = workers;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Master listening on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread t = new Thread(new MasterWorkerHandler(clientSocket, workers));
                t.start();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java master.MasterServer <port> <workerHost1:port1,workerHost2:port2,...>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        String[] workerDefs = args[1].split(",");

        List<WorkerInfo> workers = new ArrayList<>();
        int id = 0;
        for (String def : workerDefs) {
            String[] parts = def.split(":");
            String host = parts[0];
            int wPort = Integer.parseInt(parts[1]);
            workers.add(new WorkerInfo(id++, host, wPort));
        }

        MasterServer server = new MasterServer(port, workers);
        server.start();
    }
}

class MasterWorkerHandler implements Runnable {

    private final Socket clientSocket;
    private final List<WorkerInfo> workers;

    public MasterWorkerHandler(Socket clientSocket, List<WorkerInfo> workers) {
        this.clientSocket = clientSocket;
        this.workers = workers;
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            while (true) {
                Object obj = in.readObject();
                if (!(obj instanceof Message)) {
                    continue;
                }
                Message msg = (Message) obj;
                Message response = handleMessage(msg);
                out.writeObject(response);
                out.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            // απλή καταγραφή σφάλματος ανά σύνδεση
            System.err.println("Client handler error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }

    private Message handleMessage(Message msg) {
        String type = msg.getType();
        switch (type) {
            case "PING":
                return new Message("PONG", "OK");
            case "ADD_GAME":
                if (msg.getPayload() instanceof Game) {
                    Game g = (Game) msg.getPayload();
                    WorkerInfo w = selectWorkerForGame(g.getGameName());
                    return forwardToWorker(w, msg);
                }
                return new Message("ERROR", "Invalid payload for ADD_GAME");
            case "REMOVE_GAME":
                if (msg.getPayload() instanceof String) {
                    String gameName = (String) msg.getPayload();
                    WorkerInfo w = selectWorkerForGame(gameName);
                    Message deactivateMsg = new Message("DEACTIVATE_GAME", gameName);
                    return forwardToWorker(w, deactivateMsg);
                }
                return new Message("ERROR", "Invalid payload for REMOVE_GAME");
            case "SEARCH":
                if (msg.getPayload() instanceof SearchRequest) {
                    return searchAcrossWorkers((SearchRequest) msg.getPayload());
                }
                return new Message("ERROR", "Invalid payload for SEARCH");
            case "PLAY":
                if (msg.getPayload() instanceof PlayRequest) {
                    PlayRequest req = (PlayRequest) msg.getPayload();
                    WorkerInfo w = selectWorkerForGame(req.getGameName());
                    return forwardToWorker(w, msg);
                }
                return new Message("ERROR", "Invalid payload for PLAY");
            // TODO: UPDATE_GAME, ADD_BALANCE
            default:
                return new Message("ERROR", "Unknown message type: " + type);
        }
    }

    private WorkerInfo selectWorkerForGame(String gameName) {
        int hash = Math.abs(gameName.hashCode());
        int idx = hash % workers.size();
        return workers.get(idx);
    }

    private Message forwardToWorker(WorkerInfo workerInfo, Message msg) {
        try (Socket socket = new Socket(workerInfo.getHost(), workerInfo.getPort());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(msg);
            out.flush();
            Object resp = in.readObject();
            if (resp instanceof Message) {
                return (Message) resp;
            }
            return new Message("ERROR", "Invalid response from worker");
        } catch (Exception e) {
            return new Message("ERROR", "Failed to contact worker: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Message searchAcrossWorkers(SearchRequest req) {
        List<Game> all = new ArrayList<>();
        for (WorkerInfo w : workers) {
            Message resp = forwardToWorker(w, new Message("SEARCH", req));
            if ("SEARCH_RESULT".equals(resp.getType()) && resp.getPayload() instanceof List) {
                all.addAll((List<Game>) resp.getPayload());
            }
        }
        return new Message("SEARCH_RESULT", all);
    }
}

class WorkerInfo {
    private final int id;
    private final String host;
    private final int port;

    public WorkerInfo(int id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}

