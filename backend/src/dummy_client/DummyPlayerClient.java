package dummy_client;

import shared.ErrorResponse;
import shared.GameSummary;
import shared.Message;
import shared.MessageSource;
import shared.MessageType;
import shared.PlayRequest;
import shared.PlayResultResponse;
import shared.SearchRequest;
import shared.SearchResultResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
                System.out.println("2. Search games");
                System.out.println("3. Play game");
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
                        + " | stars=" + game.getStars()
                        + " | risk=" + game.getRiskLevel()
                        + " | bet=" + game.getBetCategory());
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
}
