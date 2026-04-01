package worker;

import shared.AckResponse;
import shared.BetCategory;
import shared.ErrorResponse;
import shared.Game;
import shared.GameSummary;
import shared.MapSearchResult;
import shared.Message;
import shared.MessageSource;
import shared.MessageType;
import shared.PlayRequest;
import shared.PlayResultResponse;
import shared.RemoveGameRequest;
import shared.RiskLevel;
import shared.SearchRequest;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class WorkerThread extends Thread {

    private final Socket socket;
    private final String srgHost;
    private final int srgPort;

    public WorkerThread(Socket socket, String srgHost, int srgPort) {
        this.socket = socket;
        this.srgHost = srgHost;
        this.srgPort = srgPort;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            Message msg = (Message) in.readObject();
            switch (msg.getType()) {
                case STORE_GAME:
                    storeGame(msg, out);
                    break;
                case REMOVE_GAME_LOCAL:
                    deactivateGame(msg, out);
                    break;
                case PLAY_LOCAL:
                    executePlay(msg, out);
                    break;
                case SEARCH_LOCAL:
                    executeSearch(msg, out);
                    break;
                default:
                    out.writeObject(new ErrorResponse("UNSUPPORTED", "Worker unsupported type: " + msg.getType()));
                    out.flush();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void storeGame(Message msg, ObjectOutputStream out) throws IOException {
        Game game = (Game) msg.getPayload();
        synchronized (WorkerMain.gamesStorage) {
            WorkerMain.gamesStorage.put(game.getGameName(), game);
        }

        RandomBuffer rb = new RandomBuffer(50);
        synchronized (WorkerMain.buffers) {
            WorkerMain.buffers.put(game.getGameName(), rb);
        }
        new Thread(new RandomProducer(rb, srgHost, srgPort, game.getHashKey())).start();

        String backupHost = "localhost";
        int backupPort = 8083;
        replicateToBackuo(game, backupHost, backupPort);

        out.writeObject(new Message(MessageType.ACK, MessageSource.WORKER, "Game stored and replicated."));
        out.flush();
    }

    private void deactivateGame(Message msg, ObjectOutputStream out) throws IOException {
        RemoveGameRequest request = (RemoveGameRequest) msg.getPayload();
        Game game;
        synchronized (WorkerMain.gamesStorage) {
            game = WorkerMain.gamesStorage.get(request.getGameName());
        }

        if (game == null) {
            out.writeObject(new ErrorResponse("GAME_NOT_FOUND", "Game not found on this worker."));
            out.flush();
            return;
        }

        synchronized (game) {
            game.setActive(false);
        }

        out.writeObject(new AckResponse(true, "Game deactivated successfully."));
        out.flush();
    }

    private void executePlay(Message msg, ObjectOutputStream out) throws Exception {
        PlayRequest request = (PlayRequest) msg.getPayload();
        Game game;
        RandomBuffer buffer;

        synchronized (WorkerMain.gamesStorage) {
            game = WorkerMain.gamesStorage.get(request.getGameName());
        }
        synchronized (WorkerMain.buffers) {
            buffer = WorkerMain.buffers.get(request.getGameName());
        }

        if (game == null || buffer == null) {
            out.writeObject(new Message(MessageType.ERROR, MessageSource.WORKER, "Game not found on this Worker"));
            out.flush();
            return;
        }

        double winAmount;
        boolean isJackpot = false;

        synchronized (game) {
            if (!game.isActive()) {
                out.writeObject(new Message(MessageType.ERROR, MessageSource.WORKER, "Game is inactive"));
                out.flush();
                return;
            }

            int randomNumber = buffer.take();
            double betAmount = request.getAmount();

            if (randomNumber % 100 == 0) {
                winAmount = betAmount * game.getJackpot();
                isJackpot = true;
            } else {
                int index = randomNumber % 10;
                double multiplier = getMultiplier(game.getRiskLevel(), index);
                winAmount = betAmount * multiplier;
            }

            double systemDelta = betAmount - winAmount;
            game.addToTotalProfitLoss(systemDelta);
        }

        double playerDelta = winAmount - request.getAmount();
        double newBalance;
        synchronized (WorkerMain.playerStats) {
            double currentStats = WorkerMain.playerStats.getOrDefault(request.getPlayerId(), 0.0);
            newBalance = currentStats + playerDelta;
            WorkerMain.playerStats.put(request.getPlayerId(), newBalance);
        }

        String resultMsg = isJackpot ? "JACKPOT! " : "";
        resultMsg += "You won: " + winAmount + " FUN";

        PlayResultResponse response = new PlayResultResponse(
                winAmount > request.getAmount(),
                winAmount,
                newBalance,
                resultMsg
        );
        out.writeObject(new Message(MessageType.PLAY_RESULT, MessageSource.WORKER, response));
        out.flush();
    }

    private void executeSearch(Message msg, ObjectOutputStream out) throws IOException {
        SearchRequest filters = (SearchRequest) msg.getPayload();
        List<GameSummary> matchedGames = new ArrayList<>();

        synchronized (WorkerMain.gamesStorage) {
            for (Game game : WorkerMain.gamesStorage.values()) {
                if (matchesFilters(game, filters)) {
                    matchedGames.add(toSummary(game));
                }
            }
        }

        String reducerHost = "localhost";
        int reducerPort = 8082;
        String jobId = "search:" + filters.hashCode();

        try (Socket reducerSocket = new Socket(reducerHost, reducerPort);
             ObjectOutputStream reducerOut = new ObjectOutputStream(reducerSocket.getOutputStream());
             ObjectInputStream reducerIn = new ObjectInputStream(reducerSocket.getInputStream())) {

            MapSearchResult partial = new MapSearchResult(jobId, matchedGames);
            Message mapResult = new Message(MessageType.SEARCH_RESULT_PARTIAL, MessageSource.WORKER, partial);
            reducerOut.writeObject(mapResult);
            reducerOut.flush();
            reducerIn.readObject();

            out.writeObject(new AckResponse(true, "Search partial sent to reducer."));
            out.flush();
        } catch (Exception e) {
            out.writeObject(new ErrorResponse("REDUCER_UNAVAILABLE", "Error connecting to Reducer: " + e.getMessage()));
            out.flush();
        }
    }

    private boolean matchesFilters(Game game, SearchRequest filters) {
        synchronized (game) {
            if (!game.isActive()) return false;
            if (filters.getMinStars() != null && game.getStars() < filters.getMinStars()) return false;
            if (filters.getRiskLevel() != null && !game.getRiskLevel().equalsIgnoreCase(filters.getRiskLevel().name())) return false;
            if (filters.getBetCategory() != null && !matchesBetCategory(game.getBetCategory(), filters.getBetCategory())) return false;
            return true;
        }
    }

    private boolean matchesBetCategory(String gameBetCategory, BetCategory requestBetCategory) {
        switch (requestBetCategory) {
            case LOW_BET:
                return "$".equals(gameBetCategory);
            case MEDIUM_BET:
                return "$$".equals(gameBetCategory);
            case HIGH_BET:
                return "$$$".equals(gameBetCategory);
            default:
                return false;
        }
    }

    private GameSummary toSummary(Game game) {
        synchronized (game) {
            GameSummary summary = new GameSummary();
            summary.setGameName(game.getGameName());
            summary.setMinBet(game.getMinBet());
            summary.setMaxBet(game.getMaxBet());
            summary.setStars(game.getStars());
            summary.setRiskLevel(RiskLevel.valueOf(game.getRiskLevel().toUpperCase()));
            summary.setBetCategory(toBetCategory(game.getBetCategory()));
            summary.setJackpot(game.getJackpot() > 0);
            return summary;
        }
    }

    private BetCategory toBetCategory(String gameBetCategory) {
        if ("$".equals(gameBetCategory)) {
            return BetCategory.LOW_BET;
        }
        if ("$$".equals(gameBetCategory)) {
            return BetCategory.MEDIUM_BET;
        }
        return BetCategory.HIGH_BET;
    }

    private double getMultiplier(String riskLevel, int index) {
        double[] lowTable = {0.0, 0.0, 0.0, 0.1, 0.5, 1.0, 1.1, 1.3, 2.0, 2.5};
        double[] medTable = {0.0, 0.0, 0.0, 0.0, 0.0, 0.5, 1.0, 1.5, 2.5, 3.5};
        double[] highTable = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 2.0, 6.5};

        switch (riskLevel.toLowerCase()) {
            case "low":
                return lowTable[index];
            case "medium":
                return medTable[index];
            case "high":
                return highTable[index];
            default:
                return 0.0;
        }
    }

    private void replicateToBackuo(Game game, String host, int port) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeObject(new Message(MessageType.REPLICATE_GAME, MessageSource.WORKER, game));
            out.flush();
            System.out.println("Replication successful for game: " + game.getGameName());
        } catch (IOException e) {
            System.out.println("Backup Worker is offline. Replication failed.");
        }
    }

    private void sendGameLogo(String gameName, ObjectOutputStream out) throws IOException {
        File logoFile = new File("logos/" + gameName.toLowerCase() + ".png");

        if (!logoFile.exists()) {
            out.writeObject(new Message(MessageType.ERROR, MessageSource.WORKER, "Logo not found"));
            return;
        }

        byte[] imageBytes = Files.readAllBytes(logoFile.toPath());
        out.writeObject(new Message(MessageType.LOGO_RESPONSE, MessageSource.WORKER, imageBytes));
        out.flush();
    }
}
