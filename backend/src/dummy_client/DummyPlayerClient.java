package dummy_client;

import shared.ErrorResponse;
import shared.GameSummary;
import shared.Message;
import shared.MessageSource;
import shared.MessageType;
import shared.PlayRequest;
import shared.PlayResultResponse;
import shared.RateGameRequest;
import shared.SearchRequest;
import shared.SearchResultResponse;
import shared.BetCategory;
import shared.RiskLevel;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class DummyPlayerClient {

    public static void main(String[] args) throws Exception {
        String host = (args.length >= 1) ? args[0] : "localhost";
        int port = (args.length >= 2) ? Integer.parseInt(args[1]) : 8080;

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n--- Dummy Player Menu ---");
                System.out.println("1. Ping master");
                System.out.println("2. Search all games");
                System.out.println("3. Play game");
                System.out.println("4. Search by filters");
                System.out.println("5. Game voting");
                System.out.println("0. Exit");
                System.out.print("Choice: ");

                int choice = Integer.parseInt(scanner.nextLine());

                if (choice == 0) {
                    System.out.println("Exiting...");
                    break;
                }

                Message requestMessage;

                switch (choice) {
                    case 1:
                        requestMessage = new Message(MessageType.PING, MessageSource.PLAYER, null);
                        break;
                    case 2:
                        requestMessage = new Message(MessageType.SEARCH_GAMES, MessageSource.PLAYER, new SearchRequest());
                        break;
                    case 3:
                        System.out.print("Player ID: ");
                        String playerId = scanner.nextLine();
                        System.out.print("Game name: ");
                        String gameName = scanner.nextLine();
                        System.out.print("Bet amount: ");
                        double bet = Double.parseDouble(scanner.nextLine());

                        PlayRequest preq = new PlayRequest(playerId, gameName, bet);
                        requestMessage = new Message(MessageType.PLAY_GAME, MessageSource.PLAYER, preq);
                        break;
                    case 4:
                        try {
                            requestMessage = new Message(MessageType.SEARCH_GAMES, MessageSource.PLAYER, readSearchFilters(scanner));
                        } catch (IllegalArgumentException | InputMismatchException e) {
                            System.out.println("[INPUT ERROR] " + e.getMessage());
                            continue;
                        }
                        break;
                    case 5:
                        try {
                            requestMessage = new Message(MessageType.RATE_GAME, MessageSource.PLAYER, readRateGameRequest(scanner));
                        } catch (IllegalArgumentException e) {
                            System.out.println("[INPUT ERROR] " + e.getMessage());
                            continue;
                        }
                        break;
                    default:
                        System.out.println("Invalid choice");
                        continue;
                }

                try (Socket socket = new Socket(host, port);
                     ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                     ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                    out.writeObject(requestMessage);
                    out.flush();

                    Object response = in.readObject();
                    printResponse(response);

                } catch (Exception e) {
                    System.out.println("Error connecting to Master: " + e.getMessage());
                }
            }
        }
    }

    private static void printResponse(Object response) {
        if (response instanceof Message) {
            Message message = (Message) response;
            if (message.getType() == MessageType.PLAY_RESULT) {
                PlayResultResponse result = (PlayResultResponse) message.getPayload();
                System.out.println("Play result: " + result.getMessage());
                System.out.println("Payout: " + result.getPayout());
                System.out.println("New balance: " + result.getNewBalance());
                System.out.println("Win: " + result.isWin());
                return;
            }
            System.out.println("[MASTER RESPONSE]: " + message.getPayload());
            return;
        }

        if (response instanceof SearchResultResponse) {
            SearchResultResponse searchResult = (SearchResultResponse) response;
            List<GameSummary> games = searchResult.getGames();
            if (games == null || games.isEmpty()) {
                System.out.println("No games found.");
                return;
            }

            System.out.println("Found games:");
            for (GameSummary game : games) {
                System.out.println("- " + game.getGameName()
                        + " | provider=" + game.getProviderName()
                        + " | stars=" + String.format("%.2f", game.getStars())
                        + " | risk=" + game.getRiskLevel()
                        + " | bet=" + game.getBetCategory()
                        + " | minBet=" + game.getMinBet()
                        + " | maxBet=" + game.getMaxBet());
            }
            return;
        }

        if (response instanceof ErrorResponse) {
            ErrorResponse error = (ErrorResponse) response;
            System.out.println("[ERROR] " + error.getErrorCode() + ": " + error.getMessage());
            return;
        }

        System.out.println(String.valueOf(response));
    }

    private static SearchRequest readSearchFilters(Scanner scanner) {
        SearchRequest request = new SearchRequest();

        request.setMinStars(readOptionalMinStars(scanner));
        request.setBetCategory(readOptionalBetCategory(scanner));
        request.setRiskLevel(readOptionalRiskLevel(scanner));

        return request;
    }

    private static RateGameRequest readRateGameRequest(Scanner scanner) {
        RateGameRequest request = new RateGameRequest();

        System.out.print("Player ID: ");
        request.setPlayerId(scanner.nextLine().trim());

        System.out.print("Game name: ");
        request.setGameName(scanner.nextLine().trim());

        System.out.print("Rating (1-5): ");
        int rating = Integer.parseInt(scanner.nextLine().trim());
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        request.setRating(rating);

        return request;
    }

    private static BetCategory parseBetCategory(String value) {
        switch (value.trim().toUpperCase()) {
            case "1":
            case "$":
            case "LOW":
            case "LOW_BET":
                return BetCategory.LOW_BET;
            case "2":
            case "$$":
            case "MEDIUM":
            case "MEDIUM_BET":
                return BetCategory.MEDIUM_BET;
            case "3":
            case "$$$":
            case "HIGH":
            case "HIGH_BET":
                return BetCategory.HIGH_BET;
            default:
                throw new IllegalArgumentException("Bet category must be 1/2/3, $, $$, $$$, LOW, MEDIUM or HIGH.");
        }
    }

    private static RiskLevel parseRiskLevel(String value) {
        switch (value.trim().toUpperCase()) {
            case "1":
            case "LOW":
                return RiskLevel.LOW;
            case "2":
            case "MEDIUM":
                return RiskLevel.MEDIUM;
            case "3":
            case "HIGH":
                return RiskLevel.HIGH;
            default:
                throw new IllegalArgumentException("Risk level must be 1/2/3 or LOW/MEDIUM/HIGH.");
        }
    }

    private static Double readOptionalMinStars(Scanner scanner) {
        while (true) {
            System.out.println("Minimum stars/popularity 0-5 filter: ");
            System.out.println("    Enter = no filter");
            System.out.print("Rating: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }

            try {
                double value = Double.parseDouble(input);
                if (value < 0.0 || value > 5.0) {
                    System.out.println("[INPUT ERROR] Stars filter must be between 0 and 5.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("[INPUT ERROR] Give a numeric value for stars, for example 3.5.");
            }
        }
    }

    private static BetCategory readOptionalBetCategory(Scanner scanner) {
        while (true) {
            System.out.println("Bet category filter:");
            System.out.println("  Enter = no filter");
            System.out.println("  1 = $, 2 = $$, 3 = $$$");
            System.out.print("Choice: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }

            try {
                return parseBetCategory(input);
            } catch (IllegalArgumentException e) {
                System.out.println("[INPUT ERROR] " + e.getMessage());
            }
        }
    }

    private static RiskLevel readOptionalRiskLevel(Scanner scanner) {
        while (true) {
            System.out.println("Risk level filter:");
            System.out.println("  Enter = no filter");
            System.out.println("  1 = LOW, 2 = MEDIUM, 3 = HIGH");
            System.out.print("Choice: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }

            try {
                return parseRiskLevel(input);
            } catch (IllegalArgumentException e) {
                System.out.println("[INPUT ERROR] " + e.getMessage());
            }
        }
    }
}
