package manager_app;

import shared.AckResponse;
import shared.EditGameRequest;
import shared.ErrorResponse;
import shared.Game;
import shared.Message;
import shared.MessageSource;
import shared.MessageType;
import shared.RemoveGameRequest;
import shared.RiskLevel;
import shared.StatsResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ManagerConsole {
    private static final Path DEFAULT_GAMES_DIR = Paths.get("backend", "GamesData");

    public static void main(String[] args) throws Exception {
        String host = (args.length >= 1) ? args[0] : "localhost";
        int port = (args.length >= 2) ? Integer.parseInt(args[1]) : 8080;

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n--- Manager Menu ---");
                System.out.println("1. Add game from JSON text");
                System.out.println("2. Remove game");
                System.out.println("3. Load all JSON files from backend/GamesData");
                System.out.println("4. Game stats");
                System.out.println("5. Player stats");
                System.out.println("6. Edit game settings");
                System.out.println("0. Exit");
                System.out.print("Choice: ");

                int choice = Integer.parseInt(scanner.nextLine());

                if (choice == 0) {
                    System.out.println("Exiting...");
                    break;
                }

                switch (choice) {
                    case 1:
                        System.out.println("Enter Game JSON string:");
                        String jsonInput = scanner.nextLine();
                        Game gameFromText = parseJsonToGame(jsonInput);
                        sendRequest(host, port, new Message(MessageType.ADD_GAME, MessageSource.MANAGER, gameFromText));
                        break;
                    case 2:
                        System.out.print("Enter game name to remove: ");
                        String gameName = scanner.nextLine();
                        RemoveGameRequest removeRequest = new RemoveGameRequest();
                        removeRequest.setGameName(gameName);
                        sendRequest(host, port, new Message(MessageType.REMOVE_GAME, MessageSource.MANAGER, removeRequest));
                        break;
                    case 3:
                        loadGamesFromDirectory(host, port, DEFAULT_GAMES_DIR);
                        break;
                    case 4:
                        sendRequest(host, port, new Message(MessageType.GET_GAME_STATS, MessageSource.MANAGER, null));
                        break;
                    case 5:
                        sendRequest(host, port, new Message(MessageType.GET_PLAYER_STATS, MessageSource.MANAGER, null));
                        break;
                    case 6:
                        EditGameRequest editRequest = readEditGameRequest(scanner);
                        sendRequest(host, port, new Message(MessageType.EDIT_GAME_SETTINGS, MessageSource.MANAGER, editRequest));
                        break;
                    default:
                        System.out.println("Invalid choice");
                        break;
                }
            }
        }
    }

    private static void loadGamesFromDirectory(String host, int port, Path gamesDir) {
        List<Path> gameFiles = listJsonFiles(gamesDir);
        if (gameFiles.isEmpty()) {
            System.out.println("No JSON files found in " + gamesDir);
            return;
        }

        for (Path file : gameFiles) {
            try {
                String json = Files.readString(file, StandardCharsets.UTF_8);
                Game game = parseJsonToGame(json);
                sendRequest(host, port, new Message(MessageType.ADD_GAME, MessageSource.MANAGER, game));
            } catch (Exception e) {
                System.out.println("[LOAD ERROR] " + file.getFileName() + ": " + e.getMessage());
            }
        }
    }

    private static List<Path> listJsonFiles(Path gamesDir) {
        List<Path> files = new ArrayList<>();
        if (!Files.isDirectory(gamesDir)) {
            return files;
        }

        try {
            Files.list(gamesDir)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".json"))
                    .sorted()
                    .forEach(files::add);
        } catch (IOException e) {
            System.out.println("[LOAD ERROR] Cannot read directory " + gamesDir + ": " + e.getMessage());
        }
        return files;
    }

    private static void sendRequest(String host, int port, Message requestMessage) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(requestMessage);
            out.flush();

            Object response = in.readObject();
            System.out.println("[MASTER RESPONSE]: " + formatResponse(response));
        } catch (Exception e) {
            System.out.println("Error connecting to Master: " + e.getMessage());
        }
    }

    private static EditGameRequest readEditGameRequest(Scanner scanner) {
        EditGameRequest request = new EditGameRequest();

        System.out.print("Game name: ");
        request.setGameName(scanner.nextLine().trim());

        System.out.print("New RiskLevel (LOW/MEDIUM/HIGH or empty to keep current): ");
        String riskInput = scanner.nextLine().trim();
        if (!riskInput.isEmpty()) {
            request.setNewRiskLevel(RiskLevel.valueOf(riskInput.toUpperCase()));
        }

        System.out.print("New minBet (empty to keep current): ");
        String minBetInput = scanner.nextLine().trim();
        if (!minBetInput.isEmpty()) {
            request.setNewMinBet(Double.parseDouble(minBetInput));
        }

        System.out.print("New maxBet (empty to keep current): ");
        String maxBetInput = scanner.nextLine().trim();
        if (!maxBetInput.isEmpty()) {
            request.setNewMaxBet(Double.parseDouble(maxBetInput));
        }

        if (request.getNewRiskLevel() == null && request.getNewMinBet() == null && request.getNewMaxBet() == null) {
            throw new IllegalArgumentException("You must provide at least one field to update.");
        }

        return request;
    }

    private static Game parseJsonToGame(String json) {
        Game game = new Game();
        game.setGameName(extractString(json, "gameName"));
        game.setProviderName(extractString(json, "providerName"));
        game.setStars((int) extractNumber(json, "stars"));
        game.setNoOfVotes((int) extractNumber(json, "noOfVotes"));
        game.setGameLogo(extractString(json, "gameLogo"));
        game.setMinBet(extractNumber(json, "minBet"));
        game.setMaxBet(extractNumber(json, "maxBet"));
        game.setRiskLevel(extractString(json, "riskLevel"));
        game.setHashKey(extractString(json, "hashKey"));

        if (game.getGameName() == null || game.getGameName().isEmpty()) {
            throw new IllegalArgumentException("Invalid game JSON.");
        }

        game.recalculateDerivedFields();
        game.normalizeRatingBaseline();
        return game;
    }

    private static String formatResponse(Object response) {
        if (response instanceof Message) {
            return String.valueOf(((Message) response).getPayload());
        }

        if (response instanceof AckResponse) {
            AckResponse ack = (AckResponse) response;
            return String.valueOf(ack.getMessage());
        }

        if (response instanceof ErrorResponse) {
            ErrorResponse error = (ErrorResponse) response;
            return error.getErrorCode() + ": " + error.getMessage();
        }

        if (response instanceof StatsResponse) {
            return formatStatsResponse((StatsResponse) response);
        }

        return String.valueOf(response);
    }

    private static String formatStatsResponse(StatsResponse response) {
        if (response.getStats() == null || response.getStats().isEmpty()) {
            return response.getTitle() + ": no data available yet.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(response.getTitle()).append(":\n");
        response.getStats().forEach((key, value) ->
                builder.append(" - ").append(key).append(": ").append(value).append('\n'));

        if ("Player Stats".equals(response.getTitle())) {
            builder.append("Note: players appear after their first play; their initial tracked value starts from 5.0 FUN.\n");
        }

        return builder.toString().trim();
    }

    private static String extractString(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) {
            return "";
        }

        int colon = json.indexOf(":", idx);
        int firstQuote = json.indexOf("\"", colon + 1);
        int secondQuote = json.indexOf("\"", firstQuote + 1);
        if (colon == -1 || firstQuote == -1 || secondQuote == -1) {
            return "";
        }

        return json.substring(firstQuote + 1, secondQuote).trim();
    }

    private static double extractNumber(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) {
            return 0.0;
        }

        int colon = json.indexOf(":", idx);
        int comma = json.indexOf(",", colon + 1);
        if (comma == -1) {
            comma = json.indexOf("}", colon + 1);
        }
        if (colon == -1 || comma == -1) {
            return 0.0;
        }

        String numStr = json.substring(colon + 1, comma).trim();
        return Double.parseDouble(numStr);
    }
}
