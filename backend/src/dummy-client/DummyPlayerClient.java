package dummy_client;

import shared.Game;
import shared.Message;
import shared.PlayRequest;
import shared.PlayResult;
import shared.SearchRequest;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

/**
 * Dummy client για τον παίκτη στο Παραδοτέο Α.
 * Υποστηρίζει search() και play() μέσω Master.
 */
public class DummyPlayerClient {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java dummy_client.DummyPlayerClient <masterHost> <masterPort>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("Dummy Player Menu:");
                System.out.println("1. Ping master");
                System.out.println("2. Search games");
                System.out.println("3. Play game");
                System.out.println("0. Exit");
                System.out.print("Choice: ");
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice == 0) break;

                switch (choice) {
                    case 1:
                        out.writeObject(new Message("PING", "hello"));
                        out.flush();
                        Message pong = (Message) in.readObject();
                        System.out.println("Response: " + pong.getType() + " - " + pong.getPayload());
                        break;
                    case 2:
                        SearchRequest sreq = new SearchRequest();
                        System.out.print("Provider (empty for any): ");
                        String provider = scanner.nextLine();
                        if (!provider.isEmpty()) sreq.setProviderName(provider);
                        System.out.print("Min stars (empty for any): ");
                        String starsStr = scanner.nextLine();
                        if (!starsStr.isEmpty()) sreq.setMinStars(Integer.parseInt(starsStr));
                        System.out.print("Bet category ($/$$/$$$) (empty for any): ");
                        String cat = scanner.nextLine();
                        if (!cat.isEmpty()) sreq.setBetCategory(cat);
                        System.out.print("Risk level (low/medium/high) (empty for any): ");
                        String risk = scanner.nextLine();
                        if (!risk.isEmpty()) sreq.setRiskLevel(risk);

                        out.writeObject(new Message("SEARCH", sreq));
                        out.flush();
                        Message sresp = (Message) in.readObject();
                        if (!"SEARCH_RESULT".equals(sresp.getType())) {
                            System.out.println("Error: " + sresp.getPayload());
                            break;
                        }
                        List<Game> games = (List<Game>) sresp.getPayload();
                        if (games.isEmpty()) {
                            System.out.println("No games found.");
                        } else {
                            System.out.println("Games found:");
                            int idx = 1;
                            for (Game g : games) {
                                System.out.println(idx++ + ". " + g.getGameName() +
                                        " (provider=" + g.getProviderName() +
                                        ", stars=" + g.getStars() +
                                        ", minBet=" + g.getMinBet() +
                                        ", maxBet=" + g.getMaxBet() + ")");
                            }
                        }
                        break;
                    case 3:
                        System.out.print("Player ID: ");
                        String playerId = scanner.nextLine();
                        System.out.print("Game name: ");
                        String gameName = scanner.nextLine();
                        System.out.print("Bet amount: ");
                        double bet = Double.parseDouble(scanner.nextLine());

                        PlayRequest preq = new PlayRequest(playerId, gameName, bet);
                        out.writeObject(new Message("PLAY", preq));
                        out.flush();
                        Message presp = (Message) in.readObject();
                        if (!"PLAY_RESULT".equals(presp.getType())) {
                            System.out.println("Error: " + presp.getPayload());
                            break;
                        }
                        PlayResult result = (PlayResult) presp.getPayload();
                        System.out.println("Play result for game " + result.getGameName() +
                                ": profit/loss=" + result.getProfitLoss() +
                                (result.isJackpot() ? " (JACKPOT!)" : ""));
                        break;
                    default:
                        System.out.println("Invalid choice");
                }
            }
        }
    }
}

