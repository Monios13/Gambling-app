package dummy_client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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

                Message requestMessage = null;

                switch (choice) {
                    case 1:
                        requestMessage = new Message("PING", null);
                        break;
                    case 2:
                        System.out.print("Filter (empty for all): ");
                        String filter = scanner.nextLine();
                        // Αν έχεις SearchRequest στο shared, βάλτο εδώ, αλλιώς στέλνουμε το filter ως payload
                        requestMessage = new Message("SEARCH", filter); 
                        break;
                    case 3:
                        System.out.print("Player ID: ");
                        String playerId = scanner.nextLine();
                        System.out.print("Game name: ");
                        String gameName = scanner.nextLine();
                        System.out.print("Bet amount: ");
                        double bet = Double.parseDouble(scanner.nextLine());

                        PlayRequest preq = new PlayRequest(playerId, gameName, bet);
                        requestMessage = new Message("PLAY", preq);
                        break;
                    default:
                        System.out.println("Invalid choice");
                        continue;
                }

                // --- ΛΟΓΙΚΗ ΕΡΓΑΣΤΗΡΙΟΥ (Per-request Socket) ---
                try {
                    Socket socket = new Socket(host, port);
                    
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject(requestMessage); // Χρησιμοποιούμε writeObject λόγω του shared
                    out.flush();
                    
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    Message response = (Message) in.readObject();
                    
                    socket.close();

                    // Εμφάνιση αποτελέσματος
                    if ("PLAY_RESULT".equals(response.getType())) {
                        PlayResult result = (PlayResult) response.getPayload();
                        System.out.println("Play result for game " + result.getGameName() +
                                ": profit/loss=" + result.getProfitLoss() +
                                (result.isJackpot() ? " (JACKPOT!)" : ""));
                    } else {
                        System.out.println("[MASTER RESPONSE]: " + response.getPayload());
                    }

                } catch (Exception e) {
                    System.out.println("Error connecting to Master: " + e.getMessage());
                }
            }
        }
    }
}