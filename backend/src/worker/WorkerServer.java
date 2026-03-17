package worker;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Πολυνηματικός Worker TCP Server.
 * Αποθηκεύει games στη μνήμη και εκτελεί πονταρίσματα.
 * Για το Παραδοτέο Α υλοποιούμε βασική λειτουργικότητα χωρίς ακόμα MapReduce.
 */
public class WorkerServer {

    private final int port;

    // Αποθήκευση παιχνιδιών ανά όνομα
    private final Map<String, Game> games = new HashMap<>();

    // Buffer τυχαίων αριθμών ανά παιχνίδι (producer-consumer)
    private final Map<String, RandomBuffer> buffers = new HashMap<>();

    // SRG configuration
    private final String srgHost;
    private final int srgPort;

    // Πίνακες ρίσκου
    private static final double[] LOW =  {0.0 , 0.0 , 0.0 , 0.1 , 0.5 , 1.0 , 1.1 , 1.3 , 2.0 , 2.5};
    private static final double[] MED =  {0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.5 , 1.0 , 1.5 , 2.5 , 3.5};
    private static final double[] HIGH = {0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 2.0 , 6.5};

    public WorkerServer(int port, String srgHost, int srgPort) {
        this.port = port;
        this.srgHost = srgHost;
        this.srgPort = srgPort;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Worker listening on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread t = new Thread(new WorkerClientHandler(clientSocket, this));
                t.start();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Usage: java worker.WorkerServer <port> <srgHost> <srgPort>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        String srgHost = args[1];
        int srgPort = Integer.parseInt(args[2]);
        WorkerServer server = new WorkerServer(port, srgHost, srgPort);
        server.start();
    }

    // Βασικές επιχειρησιακές μέθοδοι με συγχρονισμό

    public synchronized void addGame(Game game) {
        games.put(game.getGameName(), game);
        // δημιουργία buffer και producer για το παιχνίδι
        RandomBuffer buffer = new RandomBuffer(50);
        buffers.put(game.getGameName(), buffer);
        Thread producer = new Thread(new RandomProducer(buffer, srgHost, srgPort, game.getHashKey()));
        producer.setDaemon(true);
        producer.start();
    }

    public synchronized Game getGame(String name) {
        return games.get(name);
    }

    public synchronized void deactivateGame(String name) {
        Game g = games.get(name);
        if (g != null) {
            g.setActive(false);
        }
    }

    public PlayResult play(PlayRequest req) {
        Game game;
        RandomBuffer buffer;
        synchronized (this) {
            game = games.get(req.getGameName());
            buffer = buffers.get(req.getGameName());
        }
        if (game == null || buffer == null || !game.isActive()) {
            return new PlayResult(req.getGameName(), req.getPlayerId(), req.getBetAmount(), 0.0, false);
        }

        int random = buffer.take();
        double bet = req.getBetAmount();

        double profitLoss;
        boolean jackpot = false;
        int mod100 = random % 100;
        if (mod100 == 0) {
            profitLoss = bet * game.getJackpot();
            jackpot = true;
        } else {
            int mod10 = random % 10;
            double[] table;
            String risk = game.getRiskLevel().toLowerCase();
            if (risk.equals("low")) table = LOW;
            else if (risk.equals("medium")) table = MED;
            else table = HIGH;
            double multiplier = table[mod10];
            profitLoss = bet * multiplier;
        }

        // Το σύστημα κερδίζει όταν ο παίκτης χάνει, άρα κέρδος συστήματος = -profitLoss για τον παίκτη
        game.addToTotalProfitLoss(-profitLoss);

        return new PlayResult(game.getGameName(), req.getPlayerId(), bet, profitLoss, jackpot);
    }

    public List<Game> search(SearchRequest req) {
        List<Game> result = new ArrayList<>();
        synchronized (this) {
            for (Game g : games.values()) {
                if (!g.isActive()) continue;
                if (req.getProviderName() != null && !req.getProviderName().equals(g.getProviderName())) continue;
                if (req.getMinStars() != null && g.getStars() < req.getMinStars()) continue;
                if (req.getBetCategory() != null && !req.getBetCategory().equals(g.getBetCategory())) continue;
                if (req.getRiskLevel() != null && !req.getRiskLevel().equalsIgnoreCase(g.getRiskLevel())) continue;
                if (req.getMinBet() != null && g.getMinBet() < req.getMinBet()) continue;
                if (req.getMaxBet() != null && g.getMaxBet() > req.getMaxBet()) continue;
                result.add(g);
            }
        }
        return result;
    }
}

class WorkerClientHandler implements Runnable {

    private final Socket socket;
    private final WorkerServer worker;

    public WorkerClientHandler(Socket socket, WorkerServer worker) {
        this.socket = socket;
        this.worker = worker;
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
                Message response = handleMessage(msg);
                out.writeObject(response);
                out.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Worker client handler error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private Message handleMessage(Message msg) {
        String type = msg.getType();
        switch (type) {
            case "ADD_GAME":
                if (msg.getPayload() instanceof Game) {
                    worker.addGame((Game) msg.getPayload());
                    return new Message("OK", "Game added");
                }
                return new Message("ERROR", "Invalid payload for ADD_GAME");
            case "DEACTIVATE_GAME":
                if (msg.getPayload() instanceof String) {
                    worker.deactivateGame((String) msg.getPayload());
                    return new Message("OK", "Game deactivated");
                }
                return new Message("ERROR", "Invalid payload for DEACTIVATE_GAME");
            case "SEARCH":
                if (msg.getPayload() instanceof SearchRequest) {
                    return new Message("SEARCH_RESULT", worker.search((SearchRequest) msg.getPayload()));
                }
                return new Message("ERROR", "Invalid payload for SEARCH");
            case "PLAY":
                if (msg.getPayload() instanceof PlayRequest) {
                    PlayResult res = worker.play((PlayRequest) msg.getPayload());
                    return new Message("PLAY_RESULT", res);
                }
                return new Message("ERROR", "Invalid payload for PLAY");
            default:
                return new Message("ERROR", "Unknown message type: " + type);
        }
    }
}

/**
 * Circular buffer με synchronized, wait, notify.
 */
class RandomBuffer {
    private final int[] buffer;
    private int head = 0;
    private int tail = 0;
    private int count = 0;

    public RandomBuffer(int size) {
        this.buffer = new int[size];
    }

    public synchronized void put(int value) {
        while (count == buffer.length) {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }
        buffer[tail] = value;
        tail = (tail + 1) % buffer.length;
        count++;
        notifyAll();
    }

    public synchronized int take() {
        while (count == 0) {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }
        int value = buffer[head];
        head = (head + 1) % buffer.length;
        count--;
        notifyAll();
        return value;
    }
}

/**
 * Producer που μιλάει με τον SRG server για να γεμίσει τον buffer.
 */
class RandomProducer implements Runnable {
    private final RandomBuffer buffer;
    private final String srgHost;
    private final int srgPort;
    private final String secret;

    public RandomProducer(RandomBuffer buffer, String srgHost, int srgPort, String secret) {
        this.buffer = buffer;
        this.srgHost = srgHost;
        this.srgPort = srgPort;
        this.secret = secret;
    }

    @Override
    public void run() {
        while (true) {
            try (Socket socket = new Socket(srgHost, srgPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                while (true) {
                    out.writeObject(new Message("RANDOM_REQUEST", secret));
                    out.flush();
                    Object obj = in.readObject();
                    if (!(obj instanceof Message)) {
                        continue;
                    }
                    Message msg = (Message) obj;
                    if (!"RANDOM_RESPONSE".equals(msg.getType())) {
                        continue;
                    }
                    String payload = (String) msg.getPayload();
                    int sep = payload.indexOf(':');
                    int number = Integer.parseInt(payload.substring(0, sep));
                    String hashFromServer = payload.substring(sep + 1);
                    String localHash = HashUtil.sha256(number + secret);
                    if (localHash.equals(hashFromServer)) {
                        buffer.put(number);
                    } else {
                        // αν δεν ταιριάζει, αγνοείται ο αριθμός
                    }
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}

class HashUtil {
    public static String sha256(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}

