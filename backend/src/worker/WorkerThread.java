package worker;

import shared.Game;
import shared.Message;
import shared.PlayRequest;
import shared.SearchRequest;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

public class WorkerThread extends Thread {

    private Socket socket;
    private String srgHost;
    private int srgPort;


    public WorkerThread(Socket socket, String srgHost, int srgPort) {
        this.socket = socket;
        this.srgHost = srgHost;
        this.srgPort = srgPort;
    }

    @Override
    public void run() {

        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());){

            Message msg = (Message) in.readObject();
            switch (msg.getType()){
                case "ADD_GAME":
                    Game game = (Game) msg.getPayload();
                    WorkerMain.gamesStorage.put(game.getGameName(), game);

                    RandomBuffer rb = new RandomBuffer(50);
                    WorkerMain.buffers.put(game.getGameName(), rb);
                    new Thread(new RandomProducer(rb, srgHost, srgPort, game.getHashKey())).start();

                    //bonus erwtima gia 4 atoma
                    String backupHost = "localhost";
                    int backupPort = 8083;
                    replicateToBackuo(game, backupHost, backupPort);

                    out.writeObject(new Message("OK", "Game Added and Replicated."));
                    break;

                case "PLAY":
                    executePlay(msg, out);
                    break;

                case "SEARCH":
                    // Map Phase: Φιλτράρισμα και αποστολή στον Reducer
                    break;

            }
        } catch (Exception  e) {

            e.printStackTrace();
        }
    }
    private void executePlay(Message msg, ObjectOutputStream out) throws Exception {
        PlayRequest request = (PlayRequest) msg.getPayload();
        String gameName = request.getGameName();
        double betAmmount = request.getBetAmount();

        Game game = WorkerMain.gamesStorage.get(gameName);
        RandomBuffer buffer = WorkerMain.buffers.get(gameName);

        if (game == null || buffer == null ){
            out.writeObject(new Message("ERROR", "Game not found on this Worker"));
            return;
        }

        int randomNumber = buffer.take();
        double winAmmount = 0;
        boolean isJackpot = false;

        if (randomNumber % 100 == 0) {
            winAmmount = betAmmount * game.getJackpot();
            isJackpot = true;
        } else {
            int index = randomNumber % 10;
            double multiplier = getMultiplier(game.getRiskLevel(), index);
            winAmmount = betAmmount * multiplier;
        }

        double systemDelta = betAmmount - winAmmount;
        double playerDelta = winAmmount - betAmmount;
        game.addToTotalProfitLoss(systemDelta);
        double currentStats = WorkerMain.playerStats.getOrDefault(request.getPlayerId(), 0.0);
        WorkerMain.playerStats.put(request.getPlayerId(), currentStats + playerDelta);

        String resultMsg;
        if (isJackpot) {
            resultMsg = "JACKPOT! ";
        } else {
            resultMsg = "";
        }
        resultMsg += "You won: " + winAmmount + " FUN";
        out.writeObject(new Message("PLAY_RESULT", resultMsg));
        out.flush();

    }

    private double getMultiplier(String risekLevel, int index) {
        double[] lowTable = {0.0, 0.0, 0.0, 0.1, 0.5, 1.0, 1.1, 1.3, 2.0, 2.5};
        double[] medTable = {0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.5 , 1.0 , 1.5 , 2.5 , 3.5};
        double[] highTable = {0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 2.0 , 6.5};

        switch (risekLevel.toLowerCase()) {
            case "low": return lowTable[index];
            case "medium": return medTable[index];
            case "high": return highTable[index];
            default: return 0.0;
        }
    }

    private void executeSearch(Message msg) {
        SearchRequest filters = (SearchRequest) msg.getPayload();
        List<Game> matchedGames = new ArrayList<>();

        for (Game game : WorkerMain.gamesStorage.values()) {
            if (matchesFilters(game, filters)) {
                matchedGames.add(game);
            }
        }

        String reducerHost = "localhost";
        int reducerPort = 8082;

        try (Socket reducerSocket = new Socket(reducerHost, reducerPort);
            ObjectOutputStream reducerOut = new ObjectOutputStream(reducerSocket.getOutputStream())){

            Message mapResult = new Message("MAP_RESULT", matchedGames);
            reducerOut.writeObject(mapResult);
            reducerOut.flush();
            System.out.println("Map Phase complete. Results sent to Reducer.");

        } catch (IOException e ) {
            System.out.println("Error connecring to Reducer: " + e.getMessage());
        }


    }

    private boolean matchesFilters(Game g, SearchRequest f) {
        if (f.getMinStars() != 0 && g.getStars() < f.getMinStars()) return false;
        if (f.getRiskLevel() != null && !g.getRiskLevel().equalsIgnoreCase(f.getRiskLevel())) return false;
        if (f.getBetCategory() != null && !g.getBetCategory().equals(f.getBetCategory())) return false;
        // Πρόσθεσε επιπλέον φίλτρα αν χρειάζεται (π.χ. Provider) [cite: 60]
        return true;
    }


    private void replicateToBackuo(Game game, String host, int port) {
        try (Socket socket = new Socket(host, port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeObject(new Message("REPLICATE_GAME", game));
            out.flush();
            System.out.println("Replication successful for game: " + game.getGameName());
        } catch (IOException e) {
            System.out.println("Backup Worker is offline. Replication failed.");
        }
    }

    private void sendGameLogo(String gameName, ObjectOutputStream out) throws IOException {
        // Υποθέτουμε ότι οι εικόνες είναι στον φάκελο logos/ με όνομα π.χ. poker.png
        File logoFile = new File("logos/" + gameName.toLowerCase() + ".png");

        if (!logoFile.exists()) {
            out.writeObject(new Message("ERROR", "Logo not found"));
            return;
        }

        // Διάβασμα όλων των bytes της εικόνας
        byte[] imageBytes = Files.readAllBytes(logoFile.toPath());

        // Αποστολή στον Master
        out.writeObject(new Message("LOGO_RESPONSE", imageBytes));
        out.flush();
    }
}