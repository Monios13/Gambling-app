package master;

import shared.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {

    private final Socket           clientSocket;
    private final List<WorkerInfo> workers;
    private final String           reducerHost;
    private final int              reducerPort;
    private final WorkerSelector   selector;

    public ClientHandler(Socket clientSocket,
                         List<WorkerInfo> workers,
                         String reducerHost,
                         int    reducerPort) {
        this.clientSocket = clientSocket;
        this.workers      = workers;
        this.reducerHost  = reducerHost;
        this.reducerPort  = reducerPort;
        this.selector     = new WorkerSelector(workers);
    }

    @Override
    public void run() {
        String clientAddr = clientSocket.getRemoteSocketAddress().toString();
        System.out.println("[Master] Client connected: " + clientAddr);

        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream  in  = new ObjectInputStream(clientSocket.getInputStream())) {

            out.flush();

            while (true) {
                Object raw;
                try {
                    raw = in.readObject();
                } catch (EOFException | SocketException e) {
                    break;
                }

                if (!(raw instanceof Message)) {
                    System.err.println("[Master] Unexpected object from " + clientAddr);
                    continue;
                }

                Message msg = (Message) raw;
                System.out.println("[Master] Received " + msg.getType() + " from " + clientAddr);

                switch (msg.getType()) {

                    case ADD_GAME: {
                        Game game = (Game) msg.getPayload();
                        WorkerInfo target = selector.select(game.getGameName());
                        Message workerMsg = new Message(MessageType.STORE_GAME, MessageSource.MASTER, game);
                        Object reply = forwardToWorker(target, workerMsg);
                        out.writeObject(reply);
                        out.flush();
                        break;
                    }

                    case REMOVE_GAME: {
                        RemoveGameRequest req = (RemoveGameRequest) msg.getPayload();
                        WorkerInfo target = selector.select(req.getGameName());
                        Message workerMsg = new Message(MessageType.REMOVE_GAME_LOCAL, MessageSource.MASTER, req);
                        Object reply = forwardToWorker(target, workerMsg);
                        out.writeObject(reply);
                        out.flush();
                        break;
                    }

                    case EDIT_GAME_RISK: {
                        EditRiskRequest req = (EditRiskRequest) msg.getPayload();
                        WorkerInfo target = selector.select(req.getGameName());
                        Message workerMsg = new Message(MessageType.UPDATE_GAME_RISK_LOCAL, MessageSource.MASTER, req);
                        Object reply = forwardToWorker(target, workerMsg);
                        out.writeObject(reply);
                        out.flush();
                        break;
                    }

                    case SEARCH_GAMES: {
                        SearchRequest req = (SearchRequest) msg.getPayload();
                        Object reply = handleSearch(req);
                        out.writeObject(reply);
                        out.flush();
                        break;
                    }

                    case PLAY_GAME: {
                        PlayRequest req = (PlayRequest) msg.getPayload();
                        WorkerInfo target = selector.select(req.getGameName());
                        Message workerMsg = new Message(MessageType.PLAY_LOCAL, MessageSource.MASTER, req);
                        Object reply = forwardToWorker(target, workerMsg);
                        out.writeObject(reply);
                        out.flush();
                        break;
                    }

                    case ADD_BALANCE: {
                        AddBalanceRequest req = (AddBalanceRequest) msg.getPayload();
                        Message workerMsg = new Message(MessageType.ADD_BALANCE, MessageSource.MASTER, req);
                        Object reply = broadcastFirstSuccess(workerMsg);
                        out.writeObject(reply);
                        out.flush();
                        break;
                    }

                    case RATE_GAME: {
                        RateGameRequest req = (RateGameRequest) msg.getPayload();
                        WorkerInfo target = selector.select(req.getGameName());
                        Message workerMsg = new Message(MessageType.RATE_GAME_LOCAL, MessageSource.MASTER, req);
                        Object reply = forwardToWorker(target, workerMsg);
                        out.writeObject(reply);
                        out.flush();
                        break;
                    }

                    case PING: {
                        out.writeObject(new Message(MessageType.PONG, MessageSource.MASTER, "pong"));
                        out.flush();
                        break;
                    }

                    default: {
                        out.writeObject(new ErrorResponse("UNSUPPORTED", "Unsupported type: " + msg.getType()));
                        out.flush();
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("[Master] Error with client " + clientAddr + ": " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
            System.out.println("[Master] Client disconnected: " + clientAddr);
        }
    }

    private Object handleSearch(SearchRequest req) {
        int n = workers.size();

        List<Thread> mapThreads = new ArrayList<>();
        for (WorkerInfo w : workers) {
            Thread t = new Thread(() -> {
                Message searchMsg = new Message(MessageType.SEARCH_LOCAL, MessageSource.MASTER, req);
                forwardToWorker(w, searchMsg);
            }, "map-worker-" + w.getWorkerId());
            mapThreads.add(t);
            t.start();
        }
        for (Thread t : mapThreads) {
            try { t.join(); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ReduceSearchRequest reduceReq = new ReduceSearchRequest(req, n);
        Message reduceMsg = new Message(MessageType.SEARCH_GAMES, MessageSource.MASTER, reduceReq);
        return forwardToReducer(reduceMsg);
    }

    private Object forwardToWorker(WorkerInfo worker, Message msg) {
        try (Socket s = new Socket(worker.getHost(), worker.getPort());
             ObjectOutputStream wOut = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream  wIn  = new ObjectInputStream(s.getInputStream())) {

            wOut.writeObject(msg);
            wOut.flush();
            return wIn.readObject();

        } catch (Exception e) {
            System.err.println("[Master] Worker " + worker.getWorkerId()
                    + " (" + worker.getHost() + ":" + worker.getPort() + ") unreachable: "
                    + e.getMessage());
            return new ErrorResponse("WORKER_UNAVAILABLE", "Worker " + worker.getWorkerId() + " unavailable.");
        }
    }

    private Object broadcastFirstSuccess(Message msg) {
        for (WorkerInfo w : workers) {
            Object reply = forwardToWorker(w, msg);
            if (!(reply instanceof ErrorResponse)) {
                return reply;
            }
        }
        return new ErrorResponse("ALL_WORKERS_FAILED", "Operation failed on all workers.");
    }

    private Object forwardToReducer(Message msg) {
        try (Socket s = new Socket(reducerHost, reducerPort);
             ObjectOutputStream rOut = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream  rIn  = new ObjectInputStream(s.getInputStream())) {

            rOut.writeObject(msg);
            rOut.flush();
            return rIn.readObject();

        } catch (Exception e) {
            System.err.println("[Master] Reducer unreachable: " + e.getMessage());
            return new ErrorResponse("REDUCER_UNAVAILABLE", "Reducer unavailable.");
        }
    }
}